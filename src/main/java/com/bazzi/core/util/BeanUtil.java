package com.bazzi.core.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Slf4j
public final class BeanUtil {
    private static final String F_BOOLEAN = "boolean";
    private static final String F_IS = "is";
    private static final String F_GET = "get";
    private static final String F_SET = "set";

    /**
     * 获取属性的值，也可以是t对象父类属性的值
     *
     * @param field 属性
     * @param t     对象
     * @param <T>   对象类型
     * @return 属性值
     */
    public static <T> Object getValue(Field field, T t) {
        if (field == null || t == null)
            return null;
        return getValue(field.getName(), t);
    }

    /**
     * 获取属性的值，也可以是t对象父类属性的值
     *
     * @param fieldName 属性名
     * @param t         对象
     * @param <T>       对象类型
     * @return 属性值
     */
    public static <T> Object getValue(String fieldName, T t) {
        if (fieldName == null || "".equals(fieldName) || t == null)
            return null;
        Object value = null;
        try {
            Class<?> propertyType = BeanUtils.findPropertyType(fieldName, t.getClass());
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            boolean isBoolean = F_BOOLEAN.equalsIgnoreCase(propertyType.getSimpleName());
            String getMethodName = (isBoolean ? F_IS : F_GET) + firstLetter
                    + fieldName.substring(1);
            Method getMethod = BeanUtils.findDeclaredMethod(t.getClass(), getMethodName);
            if (getMethod != null)
                value = getMethod.invoke(t);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return value;
    }

    /**
     * 给t对象的field对应的属性设置value，也可以是t对象父类的属性
     *
     * @param t     对象
     * @param field 属性
     * @param value 值
     * @param <T>   泛型对象
     */
    public static <T> void setField(T t, Field field, Object value) {
        if (t == null || field == null)
            return;
        setField(t, field.getName(), value);
    }

    /**
     * 给t对象的fieldName对应的属性设置value，也可以是t对象父类的属性
     *
     * @param t         对象
     * @param fieldName 属性名
     * @param value     值
     * @param <T>       泛型对象
     */
    public static <T> void setField(T t, String fieldName, Object value) {
        if (t == null || fieldName == null || "".equals(fieldName))
            return;
        try {
            String setMethodName = F_SET + fieldName.substring(0, 1).toUpperCase()
                    + fieldName.substring(1);

            Class<?> propertyType = BeanUtils.findPropertyType(fieldName, t.getClass());
            Method setMethod = BeanUtils.findDeclaredMethod(t.getClass(), setMethodName, propertyType);
            if (setMethod != null)
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
     * @return MultiValueMap<String, Object>对象
     */
    public static <T> MultiValueMap<String, Object> beanToMultiValueMap(T t) {
        return JsonUtil.parseMultiValueMap(JsonUtil.toJsonString(t), String.class, Object.class);
    }
}
