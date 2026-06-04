package com.bazzi.pre.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
public class User implements Serializable {

    @Setter
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userName;

    private String password;

    private String mobile;

    private String avatarUrl;

    @Setter
    private Integer sex;

    @Setter
    private Integer platform;

    private String imei;

    @Setter
    private Date regTime;

    @Setter
    private Date lastLoginTime;

    public void setUserName(String userName) {
        this.userName = userName == null ? null : userName.trim();
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public void setMobile(String mobile) {
        this.mobile = mobile == null ? null : mobile.trim();
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl == null ? null : avatarUrl.trim();
    }

    public void setImei(String imei) {
        this.imei = imei == null ? null : imei.trim();
    }

}