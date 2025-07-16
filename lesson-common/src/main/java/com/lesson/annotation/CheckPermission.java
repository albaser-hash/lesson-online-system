package com.lesson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限检查注解
 * 用于标记需要权限验证的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckPermission {
    
    /**
     * 资源类型，如：forum_topic, forum_reply, qa_question, qa_answer
     */
    String resourceType();
    
    /**
     * 操作类型，如：update, delete
     */
    String operation();
    
    /**
     * 是否允许管理员操作所有资源
     * 默认为true，表示管理员可以操作所有资源
     */
    boolean allowAdmin() default true;
} 