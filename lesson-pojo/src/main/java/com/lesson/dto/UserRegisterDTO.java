package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 */
@Data
public class UserRegisterDTO implements Serializable {

    private String userName;

    private String passWord;

    private String email;

    private String phone;

    private String userType;

    private String name; // 昵称，可空
    private Integer sex; // 性别，0=保密，1=男，2=女
    private String bio; // 个人简介，可空

}
