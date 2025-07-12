package com.lesson.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册
 */
@Data
public class UserLoginDTO implements Serializable {

    private String userName;

    private String passWord;



}
