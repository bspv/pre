package com.bazzi.core.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public final class BeanUtil {

	/**
	 * 获取属性的值
	 *
	 * @param fieldName 属性名
	 * @param t         对象
	 * @param <T>       对象类型
	 * @return 属性值
	 */
	public static <T> Object getValueByField(String fieldName, T t) {
		if (fieldName == null || "".equals(fieldName) || t == null)
			return null;
		try {
			return getValue(fieldName, t.getClass().getDeclaredField(fieldName), t);
		} catch (NoSuchFieldException e) {
			log.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * 获取属性的值
	 *
	 * @param field 属性
	 * @param t     对象
	 * @param <T>   对象类型
	 * @return 属性值
	 */
	public static <T> Object getValueByField(Field field, T t) {
		if (field == null || t == null)
			return null;
		return getValue(field.getName(), field, t);
	}

	/**
	 * 获取属性的值
	 *
	 * @param fieldName 属性名
	 * @param field     属性
	 * @param t         对象
	 * @param <T>       对象类型
	 * @return 属性值
	 */
	private static <T> Object getValue(String fieldName, Field field, T t) {
		Object value = null;
		try {
			String firstLetter = fieldName.substring(0, 1).toUpperCase();
			boolean isBoolean = "boolean".equalsIgnoreCase(
					field.getType().getSimpleName());
			String getMethodName = (isBoolean ? "is" : "get") + firstLetter
					+ fieldName.substring(1);
			value = t.getClass().getMethod(getMethodName).invoke(t);
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return value;
	}
}
