package com.stu.helloserver.exception;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
//        return Result.error(500, "服务器异常：" + e.getMessage());
        return Result.error(ResultCode.ERROR);
    }
}
