package com.bazzi.pre.config;

import com.bazzi.pre.interceptor.CustomInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Slf4j
@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebConfig implements WebMvcConfigurer {

    private final CustomInterceptor commonInterceptor;
    private final DefinitionProperties definitionProperties;

    public ExchangeFilterFunction printExchangeInfo() {
        if (definitionProperties.isProduction())
            return ExchangeFilterFunction.ofRequestProcessor(Mono::just);

        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            log.info("WebClient---Request Url:{},Method:{},Headers:{}",
                    request.url(), request.method(), request.headers());

            return Mono.just(request);
        }).andThen(ExchangeFilterFunction.ofResponseProcessor(response -> {
            log.info("WebClient---Response Status: {},Headers: {}",
                    response.statusCode(), response.headers().asHttpHeaders());

            return Mono.just(response);
        }));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(commonInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/static/**",
                        "/v2/api-docs-ext",
                        "/webjars/**",
                        "/swagger-resources",
                        "/swagger-resources/configuration/ui",
                        "/**.html");
    }

    /**
     * 跨域配置
     *
     * @param registry registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")// 所有接口
                .allowedOriginPatterns("*")// 允许的源
                .allowedMethods("*")// 允许的方法，或"GET", "POST", "PUT", "DELETE", "OPTIONS"
                .allowedHeaders("*")// 允许的请求头
                .allowCredentials(true)// 允许发送 Cookie
                .maxAge(3600)// 预检请求的缓存时间（秒）
                .exposedHeaders(HttpHeaders.SET_COOKIE);
    }
}
