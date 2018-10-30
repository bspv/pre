package com.bazzi.pre.util;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Properties;

public final class ProfileUtil {
	private static final String PROFILE_ACTIVE_KEY = "spring.profiles.active";
	private static final String DEFAULT_PROFILE = "dev";
	private static final String YML_BASE_FILE = "application.yml";

	/**
	 * 获取当前激活的profile
	 *
	 * @return profile的名称
	 */
	private static String getActiveProfile() {
		// 从设置参数里获取
		String profile = System.getProperties().getProperty(PROFILE_ACTIVE_KEY);
		if (profile == null || "".equals(profile)) {
			// 从yml文件里获取
			Resource r = new ClassPathResource(YML_BASE_FILE);
			YamlPropertiesFactoryBean baseYml = new YamlPropertiesFactoryBean();
			baseYml.setResources(r);
			Properties properties = baseYml.getObject();
			profile = properties != null ? properties.getProperty(PROFILE_ACTIVE_KEY) : null;
		}
		return profile == null || "".equals(profile) ? DEFAULT_PROFILE : profile;
	}
}
