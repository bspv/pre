package com.bazzi.pre.controller;

import com.bazzi.core.generic.Result;
import com.bazzi.core.generic.TipsCodeEnum;
import com.bazzi.pre.bean.UserBean;
import com.bazzi.pre.model.User;
import com.bazzi.pre.service.UserService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dm")
@Api(tags = "用户管理接口", value = "用户管理接口")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody UserBean request) {
        try {
            User user = userService.login(request.getUsername(), request.getPassword());
            return Result.success(user);
        } catch (Exception e) {
            return Result.failure(TipsCodeEnum.CODE_0011.getCode(), e.getMessage());
        }
    }

}
