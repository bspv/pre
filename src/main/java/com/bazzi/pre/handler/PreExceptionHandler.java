package com.bazzi.pre.handler;

import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.generic.Result;
import com.bazzi.core.generic.TipsCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class PreExceptionHandler {

    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(ParameterException.class)
    public Result<String> handleException(ParameterException ex, HttpServletRequest request,
                                     HttpServletResponse response) {
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(BusinessException.class)
    public Result<String> handleException(BusinessException ex, HttpServletRequest request,
                                     HttpServletResponse response) {
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<String> handleException(HttpRequestMethodNotSupportedException ex,
                                     HttpServletRequest request, HttpServletResponse response) {
        log.error(ex.getMessage(), ex);
        return Result.failure(TipsCodeEnum.CODE_0020.getCode(), String.format(TipsCodeEnum.CODE_0020.getMessage(), ex.getMethod()));
    }

    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        log.error(ex.getMessage(), ex);
        return Result.failure(TipsCodeEnum.CODE_0011.getCode(), ex.getMessage());
    }

}
