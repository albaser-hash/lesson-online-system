package com.lesson.handler;

import com.lesson.constant.MessageConstant;
import com.lesson.exception.BaseException;
import com.lesson.exception.PermissionDeniedException;
import com.lesson.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    
    /**
     * 捕获权限异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(PermissionDeniedException ex){
        log.error("权限异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    
    //捕获SQL异常
    @ExceptionHandler
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            if (message.contains("learning_progress")) {
                return Result.error("您已学习过该章节，无需重复操作");
            }
            //给前端返回哪个用户名已存在，所以需要动态提取异常信息里的用户名
            String[] split=message.split(" ");//根据空格分隔异常信息，得到数组
            String value = split[2];//获取数组第三个元素，即用户名
            return Result.error(value + MessageConstant.ALREADY_EXIT);
        }
        return Result.error(MessageConstant.UNKNOWN_ERROR);
    }
    
    /**
     * 捕获文件上传大小超限异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(MaxUploadSizeExceededException ex){
        log.error("文件上传大小超限：{}", ex.getMessage());
        return Result.error("文件大小超过限制，最大支持2GB");
    }
    
    /**
     * 捕获文件上传异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(MultipartException ex){
        log.error("文件上传异常：{}", ex.getMessage());
        return Result.error("文件上传失败：" + ex.getMessage());
    }
}
