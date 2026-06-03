package com.bazzi.core.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.StreamException;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.DomReader;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于XStream框架、基于注解的XML解析工具类。
 * <p>
 * 设计要点：
 * <ul>
 *     <li>序列化 / CDATA 序列化 / 反序列化各保留 1 个全局 {@link XStream} 实例，避免重复创建；</li>
 *     <li>反序列化使用类型白名单，且禁用 DOCTYPE / 外部实体，规避 XXE 与不可信反序列化风险；</li>
 *     <li>序列化与反序列化共用同一个 {@link NameCoder}，保证字段名编码规则双向一致；</li>
 *     <li>CDATA 写入对 {@code ]]>} 做了转义，避免内容破坏 CDATA 块。</li>
 * </ul>
 */
public final class XmlUtil {

    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    private static final String ENCODE_UTF_8 = "UTF-8";

    //业务模型类型白名单（按需调整）
    private static final String[] ALLOWED_TYPE_WILDCARDS = {"com.bazzi.**"};

    //序列化与反序列化共用，保证 _ / - 编码规则一致。
    private static final NameCoder NAME_CODER = new XmlFriendlyNameCoder("-_", "_");

    private static final XStream SERIALIZE_XSTREAM = buildSerializeXStream();
    private static final XStream SERIALIZE_CDATA_XSTREAM = buildSerializeCDATAXStream();
    private static final XStream DESERIALIZE_XSTREAM = buildDeserializeXStream();

    //用 ConcurrentHashMap.newKeySet() 做轻量去重，避免同一类型反复调用 processAnnotations。
    private static final Set<Class<?>> SERIALIZE_REGISTERED = ConcurrentHashMap.newKeySet();
    private static final Set<Class<?>> SERIALIZE_CDATA_REGISTERED = ConcurrentHashMap.newKeySet();
    private static final Set<Class<?>> DESERIALIZE_REGISTERED = ConcurrentHashMap.newKeySet();


    private XmlUtil() {
    }

    /**
     * 将XML格式的字符串转换成对象。
     *
     * @param xml   xml格式字符串
     * @param clazz 目标类型
     * @return 目标对象，xml或clazz为空时返回null
     */
    public static <T> T fromXml(String xml, Class<T> clazz) {
        if (xml == null || xml.isEmpty() || clazz == null) {
            return null;
        }
        registerType(DESERIALIZE_XSTREAM, DESERIALIZE_REGISTERED, clazz);
        // 显式放行调用方传入的目标类型，避免被白名单拦截
        DESERIALIZE_XSTREAM.allowTypes(new Class[]{clazz});
        Object obj = DESERIALIZE_XSTREAM.fromXML(xml);
        return clazz.cast(obj);
    }

    /**
     * 将对象转换成XML格式字符串。
     */
    public static <T> String toXml(T t) {
        return toXml(t, null);
    }

    /**
     * 将对象转换成XML格式字符串。
     *
     * @param t      对象
     * @param header xml头，为空则不附加
     */
    public static <T> String toXml(T t, String header) {
        if (t == null) {
            return null;
        }
        registerType(SERIALIZE_XSTREAM, SERIALIZE_REGISTERED, t.getClass());
        String xml = SERIALIZE_XSTREAM.toXML(t);
        return (header == null || header.isEmpty()) ? xml : header + xml;
    }

    /**
     * 将对象转换成带有CDATA标签的XML格式字符串。
     */
    public static <T> String toCDATAXml(T t) {
        return toCDATAXml(t, null);
    }

    /**
     * 将对象转换成带有CDATA标签的XML格式字符串。
     *
     * @param t      对象
     * @param header xml头，为空则不附加
     */
    public static <T> String toCDATAXml(T t, String header) {
        if (t == null) {
            return null;
        }
        registerType(SERIALIZE_CDATA_XSTREAM, SERIALIZE_CDATA_REGISTERED, t.getClass());
        String xml = SERIALIZE_CDATA_XSTREAM.toXML(t);
        return (header == null || header.isEmpty()) ? xml : header + xml;
    }

    private static void registerType(XStream xs, Set<Class<?>> registry, Class<?> type) {
        if (registry.add(type)) {
            xs.processAnnotations(type);
        }
    }

    /**
     * 序列化用 XStream，无需放权限（不会反序列化外部输入）。
     */
    private static XStream buildSerializeXStream() {
        XStream xs = new XStream(new DomDriver(ENCODE_UTF_8, NAME_CODER));
        xs.ignoreUnknownElements();
        xs.setClassLoader(XmlUtil.class.getClassLoader());
        return xs;
    }

    /**
     * 序列化（CDATA）用 XStream。
     */
    private static XStream buildSerializeCDATAXStream() {
        XStream xs = new XStream(new DomDriver(ENCODE_UTF_8, NAME_CODER) {
            @Override
            public HierarchicalStreamWriter createWriter(Writer out) {
                return new PrettyPrintWriter(out, NAME_CODER) {
                    @Override
                    protected void writeText(QuickWriter writer, String text) {
                        if (text == null) {
                            return;
                        }
                        // 转义内容里出现的 ]]>，避免破坏 CDATA 块
                        String safe = text.replace("]]>", "]]]]><![CDATA[>");
                        writer.write("<![CDATA[");
                        writer.write(safe);
                        writer.write("]]>");
                    }
                };
            }
        });
        xs.ignoreUnknownElements();
        xs.setClassLoader(XmlUtil.class.getClassLoader());
        return xs;
    }

    /**
     * 反序列化用 XStream，启用类型白名单 + XXE 防护。
     */
    private static XStream buildDeserializeXStream() {
        XStream xs = new XStream(new HardenedDomDriver(ENCODE_UTF_8, NAME_CODER));

        // 关闭默认全部权限，按需放行
        xs.addPermission(NoTypePermission.NONE);
        xs.addPermission(NullPermission.NULL);
        xs.addPermission(PrimitiveTypePermission.PRIMITIVES);
        xs.allowTypeHierarchy(String.class);
        xs.allowTypeHierarchy(Number.class);
        xs.allowTypeHierarchy(Boolean.class);
        xs.allowTypeHierarchy(Date.class);
        xs.allowTypeHierarchy(Collection.class);
        xs.allowTypeHierarchy(Map.class);
        xs.allowTypesByWildcard(ALLOWED_TYPE_WILDCARDS);

        xs.ignoreUnknownElements();
        xs.setClassLoader(XmlUtil.class.getClassLoader());
        return xs;
    }

    /**
     * 加固版 DomDriver：在创建 DocumentBuilder 时禁用 DTD / 外部实体，避免 XXE。
     */
    private static final class HardenedDomDriver extends DomDriver {

        HardenedDomDriver(String encoding, NameCoder nameCoder) {
            super(encoding, nameCoder);
        }

        @Override
        public HierarchicalStreamReader createReader(Reader in) {
            return doCreateReader(new InputSource(in));
        }

        @Override
        public HierarchicalStreamReader createReader(InputStream in) {
            try {
                return createReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new StreamException(e);
            }
        }

        private HierarchicalStreamReader doCreateReader(InputSource source) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
                dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                dbf.setXIncludeAware(false);
                dbf.setExpandEntityReferences(false);
                DocumentBuilder db = dbf.newDocumentBuilder();
                return new DomReader(db.parse(source), getNameCoder());
            } catch (ConversionException e) {
                throw e;
            } catch (Exception e) {
                throw new StreamException(e);
            }
        }
    }
}
