package com.bazzi.pre.bean;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserBean {

    @NotNull(message = "用户名不能为空")
    private String username;
    private String password;
}
