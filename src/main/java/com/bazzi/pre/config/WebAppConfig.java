package com.bazzi.pre.config;

import com.bazzi.pre.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new LoginInterceptor())
				.addPathPatterns("/**").excludePathPatterns("/static/**");
	}

	/**
	 * 跨域配置
	 *
	 * @param registry registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedOrigins("*")
				.allowedMethods("*").allowedHeaders("*")
				.allowCredentials(true).maxAge(3600)
				.exposedHeaders(HttpHeaders.SET_COOKIE);
	}

}
