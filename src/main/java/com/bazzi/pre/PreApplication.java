package com.bazzi.pre;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@MapperScan("com.bazzi.pre.mapper")
@ServletComponentScan
@SpringBootApplication
public class PreApplication {
    public static void main(String[] args) {
        SpringApplication.run(PreApplication.class, args);
    }
}
