package com.lesson.aspect;

import com.lesson.annotation.CheckPermission;
import com.lesson.entity.ForumReply;
import com.lesson.entity.ForumTopics;
import com.lesson.entity.QaAnswer;
import com.lesson.entity.QaQuestion;
import com.lesson.entity.User;
import com.lesson.exception.PermissionDeniedException;
import com.lesson.mapper.ForumReplyMapper;
import com.lesson.mapper.ForumTopicsMapper;
import com.lesson.mapper.QaAnswerMapper;
import com.lesson.mapper.QaQuestionMapper;
import com.lesson.mapper.UserMapper;
import com.lesson.mapper.NotificationMapper;
import com.lesson.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.lesson.context.BaseContext.getCurrentId;

/**
 * 权限检查切面
 * 用于拦截带有@CheckPermission注解的方法并进行权限验证
 */
@Aspect
@Component
@Slf4j
public class PermissionAspect {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ForumTopicsMapper forumTopicsMapper;
    
    @Autowired
    private ForumReplyMapper forumReplyMapper;
    
    @Autowired
    private QaQuestionMapper qaQuestionMapper;
    
    @Autowired
    private QaAnswerMapper qaAnswerMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Before("@annotation(checkPermission)")
    public void checkPermission(JoinPoint joinPoint, CheckPermission checkPermission) {
        String resourceType = checkPermission.resourceType();
        String operation = checkPermission.operation();
        boolean allowAdmin = checkPermission.allowAdmin();
        
        // 获取当前用户ID
        Integer currentUserId = getCurrentId();
        if (currentUserId == null) {
            throw new PermissionDeniedException("用户未登录");
        }
        
        // 获取当前用户信息
        User currentUser = userMapper.selectById(currentUserId);
        if (currentUser == null) {
            throw new PermissionDeniedException("用户不存在");
        }
        
        // 检查是否是管理员
        boolean isAdmin = "ADMIN".equals(currentUser.getUserType());
        
        // 如果是管理员且允许管理员操作所有资源，直接放行
        if (isAdmin && allowAdmin) {
            log.info("管理员操作 - 用户ID: {}, 资源类型: {}, 操作: {}", currentUserId, resourceType, operation);
            return;
        }
        
        // 获取方法参数中的资源ID
        Integer resourceId = getResourceIdFromArgs(joinPoint.getArgs(), resourceType);
        if (resourceId == null) {
            throw new PermissionDeniedException("无法获取资源ID");
        }
        
        // 检查资源是否存在且属于当前用户
        boolean hasPermission = checkResourcePermission(resourceType, resourceId, currentUserId);
        
        if (!hasPermission) {
            log.warn("权限不足 - 用户ID: {}, 资源类型: {}, 资源ID: {}, 操作: {}", 
                    currentUserId, resourceType, resourceId, operation);
            throw new PermissionDeniedException("权限不足，无法执行此操作");
        }
        
        log.info("权限验证通过 - 用户ID: {}, 资源类型: {}, 资源ID: {}, 操作: {}", 
                currentUserId, resourceType, resourceId, operation);
    }
    
    /**
     * 从方法参数中获取资源ID
     */
    private Integer getResourceIdFromArgs(Object[] args, String resourceType) {
        for (Object arg : args) {
            if (arg instanceof Integer) {
                return (Integer) arg;
            } else if (arg != null) {
                // 处理DTO对象中的ID字段
                try {
                    switch (resourceType) {
                        case "forum_topic":
                            if (arg.getClass().getMethod("getTopicId") != null) {
                                return (Integer) arg.getClass().getMethod("getTopicId").invoke(arg);
                            }
                            break;
                        case "forum_reply":
                            if (arg.getClass().getMethod("getReplyId") != null) {
                                return (Integer) arg.getClass().getMethod("getReplyId").invoke(arg);
                            }
                            break;
                        case "qa_question":
                            if (arg.getClass().getMethod("getQuestionId") != null) {
                                return (Integer) arg.getClass().getMethod("getQuestionId").invoke(arg);
                            }
                            break;
                        case "qa_answer":
                            if (arg.getClass().getMethod("getAnswerId") != null) {
                                return (Integer) arg.getClass().getMethod("getAnswerId").invoke(arg);
                            }
                            break;
                    }
                } catch (Exception e) {
                    log.debug("获取资源ID失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }
    
    /**
     * 检查资源权限
     */
    private boolean checkResourcePermission(String resourceType, Integer resourceId, Integer currentUserId) {
        try {
            switch (resourceType) {
                case "forum_topic":
                    ForumTopics topic = forumTopicsMapper.selectById(resourceId);
                    return topic != null && topic.getUserId().equals(currentUserId);
                    
                case "forum_reply":
                    ForumReply reply = forumReplyMapper.selectById(resourceId);
                    return reply != null && reply.getUserId().equals(currentUserId);
                    
                case "qa_question":
                    QaQuestion question = qaQuestionMapper.selectById(resourceId);
                    return question != null && question.getUserId().equals(currentUserId);
                    
                case "qa_answer":
                    QaAnswer answer = qaAnswerMapper.selectById(resourceId);
                    return answer != null && answer.getUserId().equals(currentUserId);
                    
                case "notification":
                    Notification notification = notificationMapper.selectById(resourceId);
                    return notification != null && notification.getUserId().equals(currentUserId);
                    
                default:
                    log.warn("未知的资源类型: {}", resourceType);
                    return false;
            }
        } catch (Exception e) {
            log.error("检查资源权限时发生错误: {}", e.getMessage());
            return false;
        }
    }
} 