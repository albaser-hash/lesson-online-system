package com.lesson.interceptor;

import com.lesson.constant.JwtClaimsConstant;
import com.lesson.context.BaseContext;
import com.lesson.properties.JwtProperties;
import com.lesson.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    //令牌校验拦截器
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getTokenName());
        

        //2、校验令牌
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Integer userId = (Integer) claims.get(JwtClaimsConstant.USER_ID);
            BaseContext.setCurrentId(userId);
            return true;
        } catch (Exception ex) {
            log.error("JWT拦截器 - Token校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }
}
