package com.bazzi.pre;

import com.bazzi.pre.config.DefinitionProperties;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import tk.mybatis.spring.annotation.MapperScan;

import javax.annotation.Resource;
import javax.validation.Validation;
import javax.validation.Validator;
import java.text.SimpleDateFormat;
import java.time.Duration;

@MapperScan(basePackages = {"com.bazzi.pre.mapper"})
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 2000)
@EnableCaching
@ServletComponentScan
@EnableConfigurationProperties({DefinitionProperties.class})
@SpringBootApplication
public class PreApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreApplication.class, args);
	}

	@Bean
	public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
		MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
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

	@Resource
	private DefinitionProperties properties;

	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

		RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
				// 设置默认过期时间：86400s，在配置文件中可以修改
				.entryTtl(Duration.ofSeconds(properties.getCacheTTL()))
				// 设置缓存前缀的规则
				.computePrefixWith(cacheName ->
						properties.getCachePrefix().concat(":").concat(cacheName).concat(":"))
				// .disableCachingNullValues()
				// 设置Value序列化的规则
				.serializeValuesWith(RedisSerializationContext
						.SerializationPair.fromSerializer(buildRedisValueSerializer()));

		return RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(cacheConfig).build();
	}

	@Bean
	public Validator getValidatorFactory() {
		return Validation.byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory().getValidator();
	}

	private static Jackson2JsonRedisSerializer<?> buildRedisValueSerializer() {
		// 使用Jackson2JsonRedisSerialize 替换默认序列化
		Jackson2JsonRedisSerializer<?> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

		ObjectMapper objectMapper = new ObjectMapper();
		// 设置任何字段可见
		objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
		objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		// 属性为NULL不序列化
		objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

		jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
		return jackson2JsonRedisSerializer;
	}

}
