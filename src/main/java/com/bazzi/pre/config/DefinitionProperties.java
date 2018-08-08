package com.bazzi.pre.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class DefinitionProperties {

	@Value("${spring.cache.redis.key-prefix}")
	private String cachePrefix;

	@Value("${spring.cache.redis.default-time-to-live}")
	private long cacheTTL;

}
