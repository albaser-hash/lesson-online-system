package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lesson.dto.UserLoginDTO;
import com.lesson.dto.UserRegisterDTO;
import com.lesson.entity.User;
import com.lesson.exception.LoginFailedException;
import com.lesson.mapper.UserMapper;
import com.lesson.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.lesson.context.BaseContext.getCurrentId;
import static com.lesson.context.BaseContext.removeCurrentId;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper  userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    //用户注册
    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        // 检查用户名是否已存在，即用户名不能重复
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("user_name", userRegisterDTO.getUserName()));
        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 构建用户对象
        User user = new User();
        user.setUserName(userRegisterDTO.getUserName());
        user.setPassWord(passwordEncoder.encode(userRegisterDTO.getPassWord()));
        user.setEmail(userRegisterDTO.getEmail());
        user.setPhone(userRegisterDTO.getPhone());
        user.setUserType(userRegisterDTO.getUserType());
        user.setRegisterTime(Timestamp.valueOf(LocalDateTime.now()));
        user.setName(userRegisterDTO.getName());
        Integer sex = userRegisterDTO.getSex();
        if (sex == null) {
            sex = 0; // 默认保密
        }
        user.setSex(sex);
        user.setBio(userRegisterDTO.getBio());

        // 使用 MyBatis-Plus 插入数据
        userMapper.insert(user);

        return user;
    }
    //用户登录
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUserName();
        String rawPassword = userLoginDTO.getPassWord();
        // 查询用户
        User existingUser = userMapper.selectOne(
                new QueryWrapper<User>().eq("user_name", username)
        );
        if (existingUser == null) {
            throw new LoginFailedException("用户名不存在，请先注册");
        }
        // 验证密码
        if (!passwordEncoder.matches(rawPassword, existingUser.getPassWord())) {
            throw new LoginFailedException("登录失败，密码错误");
        }

        return existingUser;
    }
/*
 * 获取用户信息
 */
    @Override
    public User getUserInfo() {
        Integer currentId = getCurrentId();
        return  userMapper.selectById(currentId);
    }
/*
 * 退出登录
 */
    @Override
    public boolean logout() {
        removeCurrentId();
        return true;
    }

    @Override
    public int update(User user) {
        // 获取当前登录用户的ID
        Integer currentUserId = getCurrentId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }
        
        // 设置用户ID为当前登录用户的ID
        user.setUserId(currentUserId);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserId, currentUserId);
        return userMapper.update(user, queryWrapper);
    }

    @Override
    public User getUserById(Integer currentId) {
        return userMapper.selectById(currentId);
    }

}
