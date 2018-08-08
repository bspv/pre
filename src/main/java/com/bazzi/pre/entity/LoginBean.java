package com.bazzi.pre.entity;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

public class LoginBean {
	@NotBlank
	private String userName;
	@NotBlank
	@Length(min = 6)
	private String password;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
