package com.bazzi.pre.model;

import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {
	private static final long serialVersionUID = -111318376545013399L;
	@Id
	private Long id;

	private String userName;

	private String password;

	private String mobile;

	private String avatarUrl;

	private Integer sex;

	private Integer platform;

	private String imei;

	private Date regTime;

	private Date lastLoginTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName == null ? null : userName.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password == null ? null : password.trim();
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile == null ? null : mobile.trim();
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public void setAvatarUrl(String avatarUrl) {
		this.avatarUrl = avatarUrl == null ? null : avatarUrl.trim();
	}

	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}

	public Integer getPlatform() {
		return platform;
	}

	public void setPlatform(Integer platform) {
		this.platform = platform;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei == null ? null : imei.trim();
	}

	public Date getRegTime() {
		return regTime != null ? new Date(regTime.getTime()) : null;
	}

	public void setRegTime(Date regTime) {
		this.regTime = regTime != null ? new Date(regTime.getTime()) : null;
	}

	public Date getLastLoginTime() {
		return lastLoginTime != null ? new Date(lastLoginTime.getTime()) : null;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime != null ? new Date(lastLoginTime.getTime()) : null;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", userName=" + userName + ", password=" + password + ", mobile=" + mobile
				+ ", avatarUrl=" + avatarUrl + ", sex=" + sex + ", platform=" + platform + ", imei=" + imei
				+ ", regTime=" + regTime + ", lastLoginTime=" + lastLoginTime + "]";
	}

}
