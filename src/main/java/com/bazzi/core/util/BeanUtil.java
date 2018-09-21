package com.bazzi.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    /**
     * 给t对象的父类的fieldName对应的属性设置value
     *
     * @param t         对象
     * @param fieldName 属性名
     * @param value     值
     * @param <T>       泛型对象
     */
    public static <T> void setSuperClassField(T t, String fieldName, Object value) {
        try {
            String setMethodName = "set"
                    + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);
            Field field = t.getClass().getSuperclass().getDeclaredField(fieldName);
            Method setMethod = t.getClass().getSuperclass().
                    getDeclaredMethod(setMethodName, field.getType());
            setMethod.invoke(t, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 将普通bean转换成MultiValueMap<String, Object>
     *
     * @param t   普通bean对象
     * @param <T> bean对象的类型
     * @return MultiValueMap<String               ,               Object>对象
     */
    public static <T> MultiValueMap<String, Object> beanToMultiValueMap(T t) {
        return JsonUtil.parseMultiValueMap(JsonUtil.toJsonString(t), String.class, Object.class);
    }
}
