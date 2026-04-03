package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice //声明一个“全局异常处理/全局增强”类,并将普通的java对象转换成jason格式返回
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
     * 处理SQL异常
     * @param ex
     * @return
     */
    @ExceptionHandler//告诉Spring MVC：这个方法专门用来处理异常。
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        String message = ex.getMessage();
        if(message.contains("Duplicate entry")){
            //Duplicate entry 'zhangsan' for key 'employee.idx_username'
            String[] split = message.split(" ");
            String username = split[2];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        }else{
            return Result.error(MessageConstant.UNKNOWN_ERROR);
        }

    }

    @ExceptionHandler
    public Result exceptionHandler(Exception ex){
        log.error("系统异常", ex);
        String message = ex.getMessage();
        if(message == null || message.trim().isEmpty()){
            message = MessageConstant.UNKNOWN_ERROR;
        }
        return Result.error(message);
    }

}
