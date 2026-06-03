package com.bazzi.pre.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Resource
    private DefinitionProperties definitionProperties;

    @Bean
    public Docket createRestApi() {
        //http://localhost:9001/doc.html
        return new Docket(DocumentationType.SWAGGER_2) // 使用 OAS_30 文档类型
                .enable(!StringUtils.equals("prod", definitionProperties.getActiveProfile())) // 生产环境禁用
                .apiInfo(apiInfo())
                .globalOperationParameters(buildGlobalRequestParameters())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.bazzi.pre.controller"))
                .paths(PathSelectors.any())
                .build().pathMapping("/");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("PRE API")
                .description("PRE接口文档")
                .version("1.0")
                .build();
    }

    private List<Parameter> buildGlobalRequestParameters() {
        List<Parameter> list = new ArrayList<>();
        list.add(new ParameterBuilder()
                .name("SERIAL-NUMBER")   // header 名称
                .description("请求流水号")
                .modelRef(new ModelRef("string"))
                .parameterType("header")       // 指定为 header 类型
                .required(true).build());
        return list;
    }
}