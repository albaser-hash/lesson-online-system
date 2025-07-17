package com.lesson.controller.user;

import com.lesson.annotation.CheckPermission;
import com.lesson.constant.JwtClaimsConstant;
import com.lesson.context.BaseContext;
import com.lesson.dto.PageQueryDTO;
import com.lesson.dto.UpdatePasswordDTO;
import com.lesson.dto.UserLoginDTO;
import com.lesson.dto.UserRegisterDTO;
import com.lesson.entity.QaQuestion;
import com.lesson.entity.User;
import com.lesson.properties.JwtProperties;
import com.lesson.result.PageResult;
import com.lesson.result.Result;
import com.lesson.service.UserService;
import com.lesson.utils.JwtUtil;
import com.lesson.vo.UserLoginVO;
import com.lesson.vo.UserRegisterVO;
import io.jsonwebtoken.Claims;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@Slf4j

public class UserController {
    @Autowired
    private UserService  userService;
    @Autowired
    private JwtProperties  jwtProperties;
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<UserRegisterVO> register(@RequestBody UserRegisterDTO userRegisterDTO){//json格式，加注解
        User user = userService.register(userRegisterDTO);
        UserRegisterVO userRegisterVO = new UserRegisterVO();
        userRegisterVO.setUserId(user.getUserId());
        return Result.success(userRegisterVO);
    }
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){//json格式，加注解
        User user = userService.login(userLoginDTO);
        //生成令牌
        Map<String,Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getUserId());//主键值，把userid用常量代替
        log.info(claims.toString());
        String token=  JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
        // 新增：生成refreshToken（有效期更长，由配置文件设置）
        long refreshTtl = jwtProperties.getRefreshTtl();
        String refreshToken = JwtUtil.createJWT(jwtProperties.getSecretKey(), refreshTtl, claims);

        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setUserId(user.getUserId());
        userLoginVO.setUserName(user.getUserName());
        //还有一个头像
        userLoginVO.setToken(token);
        userLoginVO.setRefreshToken(refreshToken); // 新增
        log.info("用户登录成功");
        return Result.success(userLoginVO);
    }
    /*
     * 获取用户信息
     */
    @GetMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public Result<User> getUserInfo(){
        User user = userService.getUserInfo();
        user.setPassWord("*******");
        return Result.success(user);
    }


    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        userService.update(user);
        return Result.success("修改成功");
    }

    @PutMapping("/update/password")
    public Result<String> updatePassword(@RequestBody UpdatePasswordDTO password) {
          User u = userService.getUserById(BaseContext.getCurrentId());
          BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
          if (!encoder.matches(password.getOldPassword(), u.getPassWord())) {
              return Result.error("旧密码错误");
          }
          u.setPassWord(encoder.encode(password.getNewPassword()));
          userService.update(u);
          return Result.success("修改成功");
    }


    @PostMapping("/logout")
    @ApiOperation("登出")
    public Result<String> logout(){
        if(userService.logout()){
            return Result.success("登出成功");
        }
        return Result.error("登出失败");
    }

    @PostMapping("/refreshToken")
    @ApiOperation("刷新accessToken")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> map) {
        String refreshToken = map.get("refreshToken");
        // 校验refreshToken合法性和有效期
        try {
            // refreshToken用和accessToken不同的ttl生成，secretKey可相同
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), refreshToken);
            Integer userId = (Integer) claims.get(JwtClaimsConstant.USER_ID);
            // 检查refreshToken是否过期（parseJWT会抛异常）
            // 生成新的accessToken
            Map<String, Object> newClaims = new HashMap<>();
            newClaims.put(JwtClaimsConstant.USER_ID, userId);
            String newAccessToken = JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), newClaims);
            Map<String, String> tokenMap = new HashMap<>();
            tokenMap.put("accessToken", newAccessToken);
            return Result.success(tokenMap);
        } catch (Exception e) {
            return Result.error("refreshToken无效或已过期");
        }
    }

    @GetMapping("/name/{userId}")
    @ApiOperation("根据用户ID获取用户名")
    public Result<String> getUserNameById(@PathVariable Integer userId) {
        User user = userService.getUserById(userId);
        if (user != null) {
            return Result.success(user.getUserName());
        } else {
            return Result.error("用户不存在");
        }
    }

}
