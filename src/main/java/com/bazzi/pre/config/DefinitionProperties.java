package com.bazzi.pre.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties
public class DefinitionProperties {

	@Value("${spring.application.name}")
	private String applicationName;

}
