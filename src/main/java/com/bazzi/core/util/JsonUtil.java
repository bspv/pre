package com.bazzi.core.util;

import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.generic.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
public final class JsonUtil {
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 将对象转为json字符串
	 *
	 * @param value 待转换的对象
	 * @return json字符串
	 */
	public static String toJsonString(Object value) {
		if (value == null)
			return null;
		try {

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
//			objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException e) {
			log.error(e.getMessage(), e);
			throw new ParameterException("-1", e.getMessage());
		}
	}

	/**
	 * json字符串转对象
	 *
	 * @param context json字符串
	 * @param clazz   类型
	 * @param <T>     类型
	 * @return 返回对象
	 */
	public static <T> T parseObject(String context, Class<T> clazz) {
		if (context == null || "".equals(context) || clazz == null)
			return null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
			return objectMapper.readValue(context, clazz);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * json字符串转list集合
	 *
	 * @param context json字符串
	 * @param clazz   集合中的类型
	 * @param <T>     集合中的类型
	 * @return list集合
	 */
	public static <T> List<T> parseList(String context, Class<T> clazz) {
		if (context == null || "".equals(context) || clazz == null)
			return null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
			JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, clazz);
			return objectMapper.readValue(context, javaType);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * json字符串转map对象
	 *
	 * @param context  json字符串
	 * @param keyClazz key类型
	 * @param valClazz value类型
	 * @param <K>      key类型
	 * @param <V>      value类型
	 * @return map对象
	 */
	public static <K, V> Map<K, V> parseMap(String context, Class<K> keyClazz, Class<V> valClazz) {
		if (context == null || "".equals(context) || keyClazz == null || valClazz == null)
			return null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
			JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Map.class, keyClazz, valClazz);
			return objectMapper.readValue(context, javaType);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 将json转换成Result<T>对象
	 *
	 * @param context json字符串
	 * @param clazz   泛型类型
	 * @param <T>     泛型类型
	 * @return Result<T>对象
	 */
	public static <T extends Serializable> Result<T> parseResult(String context, Class<T> clazz) {
		if (context == null || "".equals(context) || clazz == null)
			return null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setDateFormat(new SimpleDateFormat(DEFAULT_DATE_FORMAT));
			JavaType javaType = objectMapper.getTypeFactory().constructParametricType(Result.class, clazz);
			return objectMapper.readValue(context, javaType);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

}
