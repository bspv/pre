package com.bazzi.pre.tests.service;

import com.bazzi.pre.service.UserService;
import com.bazzi.pre.tests.TestBase;
import org.junit.Test;

import javax.annotation.Resource;

public class TestUserService extends TestBase {
	@Resource
	private UserService userService;

	@Test
	public void testFindPage() {
		print(userService.findPage(2,3));
	}
}
