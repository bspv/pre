package com.bazzi.pre.controller;

import com.bazzi.core.annotation.AllowAccess;
import com.bazzi.core.ex.BusinessException;
import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.generic.Result;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class CommonController implements ErrorController {
	private static final String ERROR_VIEW_NAME = "common/error";
	private static final String LOGIN_VIEW_NAME = "common/login";
	private static final String ERROR_MESSAGE = "message";

	/**
	 * 异常页面
	 *
	 * @param request  请求
	 * @param ex       异常
	 * @param response 响应
	 * @return html网页数据
	 */
	@AllowAccess
	@RequestMapping(path = "/error", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView errorHtml(HttpServletRequest request, Exception ex, HttpServletResponse response) {
		ModelAndView modelAndView = new ModelAndView(ERROR_VIEW_NAME);
		int status = response.getStatus();
		if (status == HttpStatus.OK.value()) {// 异常
			Object obj = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
			if (obj == null) {
				obj = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
			}
			if (obj != null && Exception.class.isAssignableFrom(obj.getClass())) {
				ex = (Exception) obj;
			}
			modelAndView.addObject(ERROR_MESSAGE, ex.getMessage());
		} else {// 403、404、500等错误
			response.setStatus(HttpStatus.OK.value());//设置200是防止微信拦截404
			modelAndView.addObject(ERROR_MESSAGE, status + HttpStatus.valueOf(status).getReasonPhrase());
		}
		return modelAndView;
	}

	/**
	 * 异常JSON
	 *
	 * @param request  请求
	 * @param ex       异常
	 * @param response 响应
	 * @return json格式数据
	 */
	@AllowAccess
	@ResponseBody
	@RequestMapping(path = "/error")
	public Result<?> errorJson(HttpServletRequest request, Exception ex, HttpServletResponse response) {
		Result<?> result = new Result<>();
		int status = response.getStatus();
		if (status == HttpStatus.OK.value()) {// 异常
			Object obj = request.getAttribute(DispatcherServlet.EXCEPTION_ATTRIBUTE);
			if (obj == null) {
				obj = request.getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE);
			}
			String code = "-1", message = null;
			if (obj != null) {
				if (BusinessException.class.isAssignableFrom(obj.getClass())) {
					BusinessException businessException = (BusinessException) obj;
					code = businessException.getCode();
					message = businessException.getMessage();
				} else if (ParameterException.class.isAssignableFrom(obj.getClass())) {
					ParameterException parameterException = (ParameterException) obj;
					code = parameterException.getCode();
					message = parameterException.getMessage();
				} else {
					Exception exception = (Exception) obj;
					message = exception.getMessage();
					message = message == null ? exception.getCause().getMessage() : message;
				}
			}
			result.setError(code, message);
		} else {// 403、404、500等错误
			result.setError(String.valueOf(status), HttpStatus.valueOf(status).getReasonPhrase());
		}
		return result;
	}

	/**
	 * html登录页面
	 *
	 * @param request  请求
	 * @param response 响应
	 * @return 登录页面
	 */
	@AllowAccess
	@RequestMapping(path = "/user/toLogin", produces = MediaType.TEXT_HTML_VALUE)
	public ModelAndView loginHtml(HttpServletRequest request, HttpServletResponse response) {
		return new ModelAndView(LOGIN_VIEW_NAME);
	}

	/**
	 * ajax登录
	 *
	 * @param request  请求
	 * @param response 响应
	 * @return 登录提示json
	 */
	@AllowAccess
	@ResponseBody
	@RequestMapping(path = "/user/toLogin")
	public Result<?> loginJson(HttpServletRequest request, HttpServletResponse response) {
		Result<?> result = new Result<>();
		result.setError("691", "请先登录！");
		return result;
	}

	@Override
	public String getErrorPath() {
		return null;
	}
}
