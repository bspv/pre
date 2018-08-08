package com.bazzi.pre.tests;

import com.bazzi.pre.PreApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Collections;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = PreApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestBase {

	@Resource
	protected TestRestTemplate testRestTemplate;

	@Before// 设置testRestTemplate编码格式
	public void beforeController() {
		testRestTemplate.getRestTemplate().setMessageConverters(
				Collections.singletonList(new StringHttpMessageConverter(Charset.defaultCharset())));
	}

	/**
	 * 以格式化json字符串形式输出obj内容
	 *
	 * @param obj 对象
	 */
	public void print(Object obj) {
		print(obj, true);
	}

	/**
	 * 以json字符串形式输出obj内容
	 *
	 * @param obj    对象
	 * @param pretty 是否需要格式化，true为是，false为否
	 */
	public static void print(Object obj, boolean pretty) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
			mapper.configure(SerializationFeature.INDENT_OUTPUT, pretty);

			//如果是json格式字符串
			if (isJSON(obj)) {
				JsonNode jsonNode = mapper.readTree(String.valueOf(obj));
				log.info(mapper.writeValueAsString(jsonNode));
				return;
			}

			//如果是普通String，则直接打印输出
			if (obj instanceof String) {
				log.info(String.valueOf(obj));
				return;
			}

			//将对象转成json字符串
			log.info(mapper.writeValueAsString(obj));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 判断obj是否为json字符串
	 *
	 * @param obj 待验证参数
	 * @return true代表是json字符串，反之返回false
	 */
	private static boolean isJSON(Object obj) {
		try {
			if (obj == null)
				return false;
			new ObjectMapper().readTree(String.valueOf(obj));
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
