package com.bazzi.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

@Slf4j
public final class DigestUtil {

	/**
	 * 将data转为对应的SHA256值
	 *
	 * @param data 数据
	 * @return sha256对应值
	 */
	public static String toSha256(String data) {
		if (data == null)
			return null;
		return DigestUtils.sha256Hex(data);
	}

	/**
	 * 将data转为对应的SHA256值，大写
	 *
	 * @param data 数据
	 * @return sha256对应值，大写
	 */
	public static String toSha256Upper(String data) {
		if (data == null)
			return null;
		return DigestUtils.sha256Hex(data).toUpperCase();
	}

	/**
	 * 计算文件的md5值
	 *
	 * @param file 待md5文件
	 * @return md5值
	 */
	public static String fileToMd5(File file) {
		if (file == null)
			return null;
		try {
			return DigestUtils.md5Hex(Files.newInputStream(file.toPath()));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 计算文件的md5值
	 *
	 * @param in 流
	 * @return md5值
	 */
	public static String fileToMd5(InputStream in) {
		if (in == null)
			return null;
		try {
			return DigestUtils.md5Hex(in);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 将data转为对应的MD5值
	 *
	 * @param data 数据
	 * @return md5对应值
	 */
	public static String toMd5(String data) {
		if (data == null)
			return null;
		return DigestUtils.md5Hex(data);
	}

	/**
	 * 将data转为对应的MD5值，大写
	 *
	 * @param data 数据
	 * @return md5对应值，大写
	 */
	public static String toMd5Upper(String data) {
		if (data == null)
			return null;
		return DigestUtils.md5Hex(data).toUpperCase();
	}

	/**
	 * Base64编码，字符串转为byte[]
	 *
	 * @param base64String 待base64的字符串
	 * @return base64后的byte数组
	 */
	public static byte[] encodeBase64(String base64String) {
		if (base64String == null)
			return null;
		return Base64.encodeBase64(StringUtils.getBytesUtf8(base64String));
	}

	/**
	 * Base64编码，字符串转为字符串
	 *
	 * @param base64String 待base64的字符串
	 * @return base64后的字符串
	 */
	public static String encodeBase64String(String base64String) {
		if (base64String == null)
			return null;
		return Base64.encodeBase64String(StringUtils.getBytesUtf8(base64String));
	}

	/**
	 * Base64编码，byte[]转为byte[]
	 *
	 * @param base64 待base64的byte数组
	 * @return base64后的byte数组
	 */
	public static byte[] encodeBase64(byte[] base64) {
		if (base64 == null)
			return null;
		return Base64.encodeBase64(base64);
	}

	/**
	 * Base64编码，byte[]转为字符串
	 *
	 * @param base64 待base64的byte数组
	 * @return base64后的字符串
	 */
	public static String encodeBase64String(byte[] base64) {
		if (base64 == null)
			return null;
		return Base64.encodeBase64String(base64);
	}

	/**
	 * Base64解码，字符串转为byte[]
	 *
	 * @param base64String 待base64解码的字符串
	 * @return base64解码后的byte数组
	 */
	public static byte[] decodeBase64(String base64String) {
		if (base64String == null)
			return null;
		return Base64.decodeBase64(base64String);
	}

	/**
	 * Base64解码，字符串转为字符串
	 *
	 * @param base64String 待base64解码的字符串
	 * @return base64解码后的字符串
	 */
	public static String decodeBase64String(String base64String) {
		if (base64String == null)
			return null;
		return StringUtils.newStringUtf8(Base64.decodeBase64(base64String));
	}

	/**
	 * Base64解码，byte[]转为byte[]
	 *
	 * @param base64 待base64解码的byte数组
	 * @return base64解码后的byte数组
	 */
	public static byte[] decodeBase64(byte[] base64) {
		if (base64 == null)
			return null;
		return Base64.decodeBase64(base64);
	}

	/**
	 * Base64解码，byte[]转为字符串
	 *
	 * @param base64 待base64解码的byte数组
	 * @return base64解码后的字符串
	 */
	public static String decodeBase64String(byte[] base64) {
		if (base64 == null)
			return null;
		return StringUtils.newStringUtf8(Base64.decodeBase64(base64));
	}

}
