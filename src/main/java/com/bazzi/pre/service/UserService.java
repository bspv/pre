package com.bazzi.pre.service;

import com.bazzi.pre.model.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface UserService {

	User findUserById(Long id);

	List<User> findUserPage(int idx, int pageSize);

	PageInfo<User> findPage(int idx, int pageSize);

}
