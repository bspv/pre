package com.bazzi.tests.util;

import com.bazzi.core.util.XmlUtil;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证重构后的 XmlUtil：
 * 1. 基本序列化/反序列化往返
 * 2. CDATA 序列化、{@code ]]>} 的转义
 * 3. _ / - 字段名编码在 toXml/fromXml 之间一致
 * 4. XXE 防护：含外部实体的 XML 不会被加载
 * 5. 反序列化类型白名单：不在白名单里的类型会被拒绝
 * 6. 入参 null/空字符串、header 拼接等边界
 * 7. 多次调用线程安全且复用同一份 XStream（间接通过往返一致性验证）
 */
class TestXmlUtil {

    // -------------------- 1. 基本往返 --------------------

    @Test
    void testToXmlAndFromXml_roundTrip() {
        User user = new User("alice", 18, "alice@example.com");
        String xml = XmlUtil.toXml(user, XmlUtil.XML_HEADER);

        assertTrue(xml.startsWith("<?xml"), "应包含 xml 头");
        assertTrue(xml.contains("<user>"), "根节点应使用 @XStreamAlias 指定的名称");

        User parsed = XmlUtil.fromXml(xml, User.class);
        assertEquals(user, parsed);
    }

    @Test
    void testToXml_withoutHeader() {
        User user = new User("bob", 20, "bob@example.com");
        String xml = XmlUtil.toXml(user);
        assertNotNull(xml);
        // 不显式传 header 时不会附加 <?xml ...?>
        assertTrue(xml.startsWith("<user>"), "未传 header 不应包含 xml 头, 实际: " + xml);
    }

    @Test
    void testNullAndEmptyInput() {
        assertNull(XmlUtil.toXml(null));
        assertNull(XmlUtil.toXml(null, XmlUtil.XML_HEADER));
        assertNull(XmlUtil.toCDATAXml(null));
        assertNull(XmlUtil.fromXml(null, User.class));
        assertNull(XmlUtil.fromXml("", User.class));
        assertNull(XmlUtil.fromXml("<user/>", null));
    }

    // -------------------- 2. CDATA --------------------

    @Test
    void testToCDATAXml_wrapsTextNodes() {
        User user = new User("alice", 18, "alice@example.com");
        String xml = XmlUtil.toCDATAXml(user);

        assertTrue(xml.contains("<![CDATA[alice]]>"),
                "字符串字段应包裹 CDATA, 实际: " + xml);
        assertTrue(xml.contains("<![CDATA[alice@example.com]]>"));
    }

    @Test
    void testToCDATAXml_escapesCdataEnd() {
        // 内容里包含 ]]> 时，必须被拆分以防破坏 CDATA 块
        User user = new User("evil]]>name", 1, "x@x.com");
        String xml = XmlUtil.toCDATAXml(user);

        assertTrue(xml.contains("]]]]><![CDATA[>"),
                "CDATA 内出现 ]]> 应被转义, 实际: " + xml);
        // 关键：转义后 XML 仍能被解析回原值
        User parsed = XmlUtil.fromXml(xml, User.class);
        assertEquals("evil]]>name", parsed.getName());
    }

    // -------------------- 3. NameCoder 双向一致 --------------------

    @Test
    void testFieldNameWithUnderscoreAndHyphen_roundTrip() {
        // 字段名同时含 _ 与 - 注解别名，验证两侧 NameCoder 一致
        Tricky obj = new Tricky();
        obj.setUser_name("u1");
        obj.setUserAge(7);

        String xml = XmlUtil.toXml(obj);
        Tricky parsed = XmlUtil.fromXml(xml, Tricky.class);

        assertEquals(obj, parsed,
                "字段含 _ / - 时应能 toXml -> fromXml 还原, xml=" + xml);
    }

    // -------------------- 4. XXE 防护 --------------------

    @Test
    void testFromXml_blocksDoctype() {
        // 反序列化器禁用了 DOCTYPE，应直接抛错而不是解析外部实体
        String malicious = "<?xml version=\"1.0\"?>" +
                "<!DOCTYPE foo [ <!ENTITY xxe SYSTEM \"file:///etc/passwd\"> ]>" +
                "<user><name>&xxe;</name><age>1</age><email>x@x.com</email></user>";
        assertThrows(Exception.class, () -> XmlUtil.fromXml(malicious, User.class),
                "包含 DOCTYPE 的 XML 应被拒绝");
    }

    // -------------------- 5. 反序列化白名单 --------------------

    @Test
    void testFromXml_rejectsTypeOutsideWhitelist() {
        // java.io.File 不在 com.bazzi.** 白名单内，反序列化应失败
        String xml = "<java.io.File>/tmp/whatever</java.io.File>";
        assertThrows(Exception.class,
                () -> XmlUtil.fromXml(xml, java.io.File.class),
                "白名单外的类型不应被反序列化");
    }

    // -------------------- 6. 集合 / 列表往返 --------------------

    @Test
    void testCollection_roundTrip() {
        Group group = new Group("dev",
                Arrays.asList(
                        new User("a", 1, "a@a.com"),
                        new User("b", 2, "b@b.com")
                ));

        String xml = XmlUtil.toXml(group, XmlUtil.XML_HEADER);
        Group parsed = XmlUtil.fromXml(xml, Group.class);

        assertEquals(group, parsed, "List 字段应支持 toXml/fromXml 往返, xml=" + xml);
    }

    // -------------------- 7. 多次调用稳定性 --------------------

    @Test
    void testRepeatedCalls_useCachedInstances() {
        User u = new User("x", 1, "x@x.com");
        String first = XmlUtil.toXml(u);
        String second = XmlUtil.toXml(u);
        // 缓存的 XStream 复用，多次调用结果应完全一致
        assertSame(first.length(), second.length());
        assertEquals(first, second);
    }

    // -------------------- 8. 属性式（@XStreamAsAttribute） --------------------

    @Test
    void testAttributeStyle_roundTrip() {
        UserAttr u = new UserAttr(1L, "alice", "alice@example.com");
        String xml = XmlUtil.toXml(u, XmlUtil.XML_HEADER);

        // 标注了 @XStreamAsAttribute 的字段渲染成属性，未标注的仍然是子元素
        assertTrue(xml.contains("id=\"1\""), "id 应渲染为属性, 实际: " + xml);
        assertTrue(xml.contains("name=\"alice\""), "name 应渲染为属性, 实际: " + xml);
        assertTrue(xml.contains("<email>alice@example.com</email>"),
                "未标注的字段仍然是子元素, 实际: " + xml);
        assertFalse(xml.contains("<id>"), "id 不应再是子元素, 实际: " + xml);

        // 反向解析仍能还原
        UserAttr parsed = XmlUtil.fromXml(xml, UserAttr.class);
        assertEquals(u, parsed);
    }

    @Test
    void testAttributeStyle_inCDATA_attributesNotWrapped() {
        UserAttr u = new UserAttr(2L, "bob", "bob@example.com");
        String xml = XmlUtil.toCDATAXml(u);

        // 属性值不允许 CDATA，CDATA 模式下属性仍然是普通形式
        assertTrue(xml.contains("id=\"2\""), "属性值不应包 CDATA, 实际: " + xml);
        assertTrue(xml.contains("name=\"bob\""));
        // 但子元素文本仍然被 CDATA 包裹
        assertTrue(xml.contains("<![CDATA[bob@example.com]]>"),
                "子元素文本仍应包 CDATA, 实际: " + xml);

        UserAttr parsed = XmlUtil.fromXml(xml, UserAttr.class);
        assertEquals(u, parsed);
    }

    // -------------------- 测试用模型 --------------------

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XStreamAlias("user")
    public static class User {
        @XStreamAlias("name")
        private String name;
        @XStreamAlias("age")
        private Integer age;
        @XStreamAlias("email")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @XStreamAlias("tricky")
    public static class Tricky {
        // 字段名含下划线
        @XStreamAlias("user_name")
        private String user_name;
        // 别名含连字符
        @XStreamAlias("user-age")
        private Integer userAge;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XStreamAlias("group")
    public static class Group {
        @XStreamAlias("name")
        private String name;
        @XStreamImplicit(itemFieldName = "user")
        private List<User> users;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XStreamAlias("user")
    public static class UserAttr {
        @XStreamAsAttribute
        @XStreamAlias("id")
        private Long id;

        @XStreamAsAttribute
        @XStreamAlias("name")
        private String name;

        @XStreamAlias("email")
        private String email;
    }
}
