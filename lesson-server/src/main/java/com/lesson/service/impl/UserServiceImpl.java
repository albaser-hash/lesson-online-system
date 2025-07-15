package com.lesson.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lesson.dto.UserLoginDTO;
import com.lesson.dto.UserRegisterDTO;
import com.lesson.entity.Chapter;
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
    @Autowired
    private com.lesson.service.OrderService orderService;
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

    @Override
    public boolean canPlayChapter(Integer userId, Chapter chapter) {
        if (userId == null || chapter == null) return false;
        // 1. 获取用户
        User user = getUserById(userId);
        if (user == null) return false;
        
        try {

            // 3. 检查videoUrl是否存在
            if (chapter.getVideoUrl() == null || chapter.getVideoUrl().trim().isEmpty()) {
                return false;
            }
            // 4. 管理员可看
            if ("ADMIN".equalsIgnoreCase(user.getUserType())) {
                return true;
            }
            if (chapter.getIsFree() == 1) {
                return true;
            }
            // 5. 老师本人可看（只用teacherId字段）
            if ("TEACHER".equalsIgnoreCase(user.getUserType()) && chapter.getTeacherId() != null && chapter.getTeacherId().equals(userId)) {
                return true;
            }
            // 6. 学生：免费章节直接放行，付费章节需已购
            if ("STUDENT".equalsIgnoreCase(user.getUserType()) && chapter.getCourseId() != null) {
                return orderService.hasPurchased(userId, chapter.getCourseId());
            }
        } catch (Exception e) {
            log.error("权限检查异常: userId={}, chapterId={}, error={}", userId, chapter.getChapterId(), e.getMessage());
            return false;
        }
        
        return false;
    }

}
