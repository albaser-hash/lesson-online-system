package com.lesson.exception;

/**
 * 业务服务异常（用于明确的业务逻辑错误提示）
 */
public class ServiceException extends BaseException {
    public ServiceException(String msg) {
        super(msg);
    }
} 