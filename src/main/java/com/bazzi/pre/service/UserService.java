package com.bazzi.pre.service;

import com.bazzi.pre.model.User;

public interface UserService {
    User login(String username, String password);
}
