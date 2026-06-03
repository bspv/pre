package com.bazzi.core.util;

import com.bazzi.core.generic.Result;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public final class JsonUtil {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String DEFAULT_TIME_ZONE = "Asia/Shanghai";

    private static final ObjectMapper OBJECT_MAPPER = buildObjectMapper();

    private JsonUtil() {
        throw new AssertionError("No JsonUtil instances for you!");
    }

    private static ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        TimeZone timeZone = TimeZone.getTimeZone(DEFAULT_TIME_ZONE);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        dateFormat.setTimeZone(timeZone);
        mapper.setDateFormat(dateFormat);
        mapper.setTimeZone(timeZone);

        // 反序列化未知字段不报错
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 日期不写成时间戳
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 显式注册 JSR-310，并通过 SPI 注册其他可用模块（Optional、Kotlin 等）
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();

        return mapper;
    }

    /**
     * 返回一个独立的 ObjectMapper 副本，供需要自定义配置的高级用法使用，
     * 防止外部修改全局共享实例。
     *
     * @return 全局 ObjectMapper 的副本
     */
    public static ObjectMapper copyObjectMapper() {
        return OBJECT_MAPPER.copy();
    }

    /**
     * 将对象转为 json 字符串。
     *
     * @param value 待转换的对象
     * @return json 字符串；当入参为 null 时返回 null
     * @throws JsonException 序列化失败时抛出
     */
    public static String toJsonString(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new JsonException("Failed to serialize object of type " + value.getClass().getName(), e);
        }
    }

    /**
     * json 字符串转对象。
     *
     * @param context json 字符串
     * @param clazz   类型
     * @param <T>     类型
     * @return 返回对象；当 context 为空或 clazz 为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <T> T parseObject(String context, Class<T> clazz) {
        if (!StringUtils.hasText(context) || clazz == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(context, clazz);
        } catch (IOException e) {
            throw newParseException(context, clazz.getName(), e);
        }
    }

    /**
     * json 字符串转对象，支持任意嵌套泛型。
     *
     * @param context json 字符串
     * @param typeRef 目标类型引用，例如 {@code new TypeReference<List<Map<String, User>>>() {}}
     * @param <T>     目标类型
     * @return 返回对象；当 context 为空或 typeRef 为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <T> T parseObject(String context, TypeReference<T> typeRef) {
        if (!StringUtils.hasText(context) || typeRef == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(context, typeRef);
        } catch (IOException e) {
            throw newParseException(context, typeRef.getType().getTypeName(), e);
        }
    }

    /**
     * json 字符串转对象，使用预先构造的 {@link JavaType}。
     *
     * @param context  json 字符串
     * @param javaType 目标 JavaType
     * @param <T>      目标类型
     * @return 返回对象；当 context 为空或 javaType 为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <T> T parseObject(String context, JavaType javaType) {
        if (!StringUtils.hasText(context) || javaType == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(context, javaType);
        } catch (IOException e) {
            throw newParseException(context, javaType.toString(), e);
        }
    }

    /**
     * json 字符串转 list 集合。
     *
     * @param context json 字符串
     * @param clazz   集合中的类型
     * @param <T>     集合中的类型
     * @return list 集合；当 context 为空或 clazz 为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <T> List<T> parseList(String context, Class<T> clazz) {
        if (!StringUtils.hasText(context) || clazz == null) {
            return Collections.emptyList();
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, clazz);
        return parseObject(context, javaType);
    }

    /**
     * json 字符串转 map 对象。
     *
     * @param context  json 字符串
     * @param keyClazz key 类型
     * @param valClazz value 类型
     * @param <K>      key 类型
     * @param <V>      value 类型
     * @return map 对象；当 context 为空或类型为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <K, V> Map<K, V> parseMap(String context, Class<K> keyClazz, Class<V> valClazz) {
        if (!StringUtils.hasText(context) || keyClazz == null || valClazz == null) {
            return Collections.emptyMap();
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(Map.class, keyClazz, valClazz);
        return parseObject(context, javaType);
    }

    /**
     * 将 Json 格式字符串转为 {@link MultiValueMap}。
     * <p>
     * 同时兼容多值与单值两种结构：
     * <ul>
     *     <li>{@code {"a":["1","2"]}} → key {@code a} 对应 value {@code [1, 2]}</li>
     *     <li>{@code {"a":"1"}}        → key {@code a} 对应 value {@code [1]}</li>
     * </ul>
     * 当输入为空时返回一个空的可变 MultiValueMap，以兼容历史调用方。
     *
     * @param context  Json 格式字符串
     * @param keyClazz key 类型
     * @param valClazz value 类型
     * @param <K>      key 类型
     * @param <V>      value 类型
     * @return MultiValueMap 对象，永不为 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <K, V> MultiValueMap<K, V> parseMultiValueMap(String context,
                                                                Class<K> keyClazz,
                                                                Class<V> valClazz) {
        MultiValueMap<K, V> result = new LinkedMultiValueMap<>();
        if (!StringUtils.hasText(context) || keyClazz == null || valClazz == null) {
            return result;
        }

        // 先按 Map<K, Object> 解析，再根据每个 value 的实际形态决定 add / addAll
        JavaType rawMapType = OBJECT_MAPPER.getTypeFactory()
                .constructParametricType(LinkedHashMap.class, keyClazz, Object.class);
        JavaType listValueType = OBJECT_MAPPER.getTypeFactory()
                .constructParametricType(List.class, valClazz);
        JavaType singleValueType = OBJECT_MAPPER.getTypeFactory().constructType(valClazz);

        Map<K, Object> raw;
        try {
            raw = OBJECT_MAPPER.readValue(context, rawMapType);
        } catch (IOException e) {
            throw newParseException(context,
                    "MultiValueMap<" + keyClazz.getName() + "," + valClazz.getName() + ">", e);
        }
        if (raw == null) {
            return result;
        }

        for (Map.Entry<K, Object> entry : raw.entrySet()) {
            K key = entry.getKey();
            Object val = entry.getValue();
            if (val == null) {
                result.add(key, null);
            } else if (val instanceof Iterable) {
                List<V> list = OBJECT_MAPPER.convertValue(val, listValueType);
                result.addAll(key, list);
            } else {
                result.add(key, OBJECT_MAPPER.convertValue(val, singleValueType));
            }
        }
        return result;
    }

    /**
     * 将 json 转换成 Result&lt;T&gt; 对象。
     * <p>
     * 注：T 必须实现 {@link Serializable}，这是 {@link Result} 类型参数自身的约束。
     *
     * @param context json 字符串
     * @param clazz   泛型类型
     * @param <T>     泛型类型
     * @return Result&lt;T&gt; 对象；当 context 为空或 clazz 为 null 时返回 null
     * @throws JsonException 反序列化失败时抛出
     */
    public static <T extends Serializable> Result<T> parseResult(String context, Class<T> clazz) {
        if (!StringUtils.hasText(context) || clazz == null) {
            return null;
        }
        JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(Result.class, clazz);
        return parseObject(context, javaType);
    }

    private static JsonException newParseException(String context, String targetType, Throwable cause) {
        // 不在 message 中输出 JSON 原文，避免敏感数据泄露；只保留长度与目标类型
        int length = context == null ? 0 : context.length();
        String msg = "Failed to parse JSON to " + targetType + " (length=" + length + ")";
        log.error(msg, cause);
        return new JsonException(msg, cause);
    }

    /**
     * JSON 处理异常，统一封装序列化/反序列化错误。
     */
    public static class JsonException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public JsonException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
