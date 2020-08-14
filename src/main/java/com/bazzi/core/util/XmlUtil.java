package com.bazzi.core.util;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import java.io.Writer;

/**
 * 基于XStream框架、基于注解的XML解析
 */
public final class XmlUtil {
	public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";

	private static final XStream cdataXStream;
	private static final XStream xStream;

	static {
		xStream = buildXStream();
		cdataXStream = buildCDATAXStream();
	}

	/**
	 * 将XML格式的字符串转换成对象，基于注解
	 *
	 * @param xml   xml格式字符串
	 * @param clazz 目标类型
	 * @return 目标对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T fromXml(String xml, Class<?> clazz) {
		if (xml == null || "".equals(xml) || clazz == null)
			return null;
		XStreamAlias annotation = clazz.getDeclaredAnnotation(XStreamAlias.class);
		String rootName = annotation == null ? clazz.getSimpleName() : annotation.value();
		String start = "<" + rootName;
		String end = "</" + rootName + ">";
		if (xml.contains(start))
			xml = xml.substring(xml.indexOf(start));
		if (xml.contains(end))
			xml = xml.substring(0, xml.indexOf(end) + end.length());
		XStream fromXStream = buildCDATAXStream();
		fromXStream.processAnnotations(clazz);
		return (T) fromXStream.fromXML(xml);
	}

	/**
	 * 将对象转换成XML格式字符串
	 *
	 * @param t 对象
	 * @return xml
	 */
	public static <T> String toXml(T t) {
		return toXml(t, null);
	}

	/**
	 * 将对象转换成XML格式字符串
	 *
	 * @param t      对象
	 * @param header header
	 * @return xml字符串
	 */
	public static <T> String toXml(T t, String header) {
		if (t == null)
			return null;
		xStream.processAnnotations(t.getClass());
		return header == null || "".equals(header) ? xStream.toXML(t) : header + xStream.toXML(t);
	}

	/**
	 * 将对象转换成带有CDATA标签的XML格式字符串
	 *
	 * @param t 对象
	 * @return 带有CDATA标签的XML格式字符串
	 */
	public static <T> String toCDATAXml(T t) {
		return toCDATAXml(t, null);
	}

	/**
	 * 将对象转换成带有CDATA标签的XML格式字符串
	 *
	 * @param t      对象
	 * @param header header
	 * @return 带有CDATA标签的XML格式字符串
	 */
	public static <T> String toCDATAXml(T t, String header) {
		if (t == null)
			return null;
		cdataXStream.processAnnotations(t.getClass());
		return header == null || "".equals(header) ? cdataXStream.toXML(t) : header + cdataXStream.toXML(t);
	}

	/**
	 * DomDriver驱动的XStream
	 *
	 * @return XStream
	 */
	private static XStream buildXStream() {
		// XmlFriendlyNameCoder解决单_变双__的问题
		XStream xs = new XStream(new DomDriver("UTF-8", new XmlFriendlyNameCoder("-_", "_")));
		XStream.setupDefaultSecurity(xs);
		xs.addPermission(AnyTypePermission.ANY);
		return xs;
	}

	/**
	 * 解析带CDATA标签的XStream
	 *
	 * @return XStream
	 */
	private static XStream buildCDATAXStream() {
		XStream xs = new XStream(new XppDriver() {
			public HierarchicalStreamWriter createWriter(Writer out) {
				return new PrettyPrintWriter(out) {
					// 对所有xml节点的转换都增加CDATA标记
					final boolean cdata = true;

					public void startNode(String name, Class clazz) {
						super.startNode(name, clazz);
					}

					// 解决单_变双__的问题
					public String encodeNode(String name) {
						return name;
					}

					protected void writeText(QuickWriter writer, String text) {
						if (cdata) {
							writer.write("<![CDATA[");
							writer.write(text);
							writer.write("]]>");
						} else {
							writer.write(text);
						}
					}
				};
			}
		});
		XStream.setupDefaultSecurity(xs);
		xs.addPermission(AnyTypePermission.ANY);
		return xs;
	}
}
