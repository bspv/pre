package com.bazzi.pre.tests.mapper;

import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.tests.TestBase;
import org.junit.Test;

import javax.annotation.Resource;

public class TestUserMapper extends TestBase {
	@Resource
	private UserMapper userMapper;

	@Test
	public void testLoadUser(){
		print(userMapper.selectByLikeKey("user_"));
	}
}
