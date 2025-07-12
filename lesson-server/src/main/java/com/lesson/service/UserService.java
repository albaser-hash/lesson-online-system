package com.lesson.service;

import com.lesson.dto.UserLoginDTO;
import com.lesson.dto.UserRegisterDTO;
import com.lesson.entity.User;

public interface UserService {
    //用户注册
    User register(UserRegisterDTO userRegisterDTO);
    //用户登录
    User login(UserLoginDTO userLoginDTO);
    //获取用户信息
    User getUserInfo();
    //登出
    boolean logout();

    int update(User user);

    User getUserById(Integer currentId);

}
