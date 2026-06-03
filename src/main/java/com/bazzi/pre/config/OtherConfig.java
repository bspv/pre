package com.bazzi.pre.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.validation.Validation;
import javax.validation.Validator;
import java.text.SimpleDateFormat;

@Configuration
public class OtherConfig {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));

        jsonConverter.setObjectMapper(objectMapper);
        return jsonConverter;
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 设置 key和 value的序列化规则
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(buildRedisValueSerializer());

        // 设置hash类型 key和 value的序列化规则
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(buildRedisValueSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public Validator getValidatorFactory() {
        return Validation.byProvider(HibernateValidator.class).configure().failFast(false).buildValidatorFactory().getValidator();
    }

    private static Jackson2JsonRedisSerializer<?> buildRedisValueSerializer() {
        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // 设置任何字段可见
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        PolymorphicTypeValidator validator = objectMapper.getPolymorphicTypeValidator();
        objectMapper.activateDefaultTyping(validator,ObjectMapper.DefaultTyping.NON_FINAL);

        objectMapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT));
        // 属性为NULL不序列化
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
        return jackson2JsonRedisSerializer;
    }

}
