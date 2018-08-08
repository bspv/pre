package com.bazzi.pre.tests.controller;

import com.bazzi.pre.tests.TestBase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TestHelloController extends TestBase {

	@Test
	public void contextLoads() {
		Map<String, String> multiValueMap = new HashMap<>();
		multiValueMap.put("age", "21");
		String result = testRestTemplate.getForObject("/idx?age={age}", String.class, multiValueMap);
		print(null,true);
		print(result,true);
		print(multiValueMap,true);
		print("这是测试json格式化的",true);
	}

	@Test
	public void testCheckParam() {
		Map<String, String> multiValueMap = new HashMap<>();
		multiValueMap.put("userName", "21");
		multiValueMap.put("password", null);
		String result = testRestTemplate.getForObject(
				"/checkParam?userName={userName}&password={password}",
				String.class, multiValueMap);
		print(result, true);
	}

	@Test
	public void testCheckP() {
		Map<String, String> multiValueMap = new HashMap<>();
		multiValueMap.put("userName", "21");
		multiValueMap.put("password", "123456");
		multiValueMap.put("age", "1");
		String result = testRestTemplate.getForObject(
				"/checkP?userName={userName}&password={password}&age={age}",
				String.class, multiValueMap);
		print(result, true);
	}

}
