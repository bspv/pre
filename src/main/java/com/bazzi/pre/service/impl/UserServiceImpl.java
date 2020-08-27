package com.bazzi.pre.service.impl;

import com.bazzi.pre.mapper.UserMapper;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.UserService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserServiceImpl implements UserService {

	@Resource
	private UserMapper userMapper;

	public User findUserById(Long id) {
		return userMapper.selectByPrimaryKey(id);
	}

	public List<User> findUserPage(int idx, int pageSize) {
		PageHelper.startPage(idx, pageSize);
		return userMapper.selectByLikeKey("user_");
	}

	public PageInfo<User> findPage(int idx, int pageSize) {
		PageHelper.startPage(idx, pageSize);
		List<User> list = userMapper.selectByLikeKey("user_");
		return PageInfo.of(list);
	}

}
