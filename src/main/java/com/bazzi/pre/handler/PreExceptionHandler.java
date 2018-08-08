package com.bazzi.pre.handler;

import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.ex.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@ControllerAdvice
public class PreExceptionHandler {

	@ResponseStatus(value = HttpStatus.OK)
	@ExceptionHandler(ParameterException.class)
	public String handleException(ParameterException ex, HttpServletRequest request,
								  HttpServletResponse response) {
		log.debug("ParameterException---CODE：{},MESSAGE：{}", ex.getCode(), ex.getMessage());
		request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
		return "forward:/error";
	}

	@ResponseStatus(value = HttpStatus.OK)
	@ExceptionHandler(BusinessException.class)
	public String handleException(BusinessException ex, HttpServletRequest request,
								  HttpServletResponse response) {
		log.info("BusinessException---CODE：{},MESSAGE：{}", ex.getCode(), ex.getMessage());
		request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
		return "forward:/error";
	}

	@ResponseStatus(value = HttpStatus.OK)
	@ExceptionHandler(Exception.class)
	public String handleException(Exception ex, HttpServletRequest request, HttpServletResponse response) {
		log.error(ex.getMessage(), ex);
		request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex);
		return "forward:/error";
	}

}
