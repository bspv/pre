package com.bazzi.pre.interceptor;

import com.bazzi.core.annotation.AllowAccess;
import com.bazzi.pre.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor extends HandlerInterceptorAdapter {
	private NamedThreadLocal<Long> timeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod) {
			boolean allowAccess = ((HandlerMethod) handler).hasMethodAnnotation(AllowAccess.class);
			User user = (User) request.getSession().getAttribute("user");
			if (!allowAccess && user == null) {
				try {
					request.getRequestDispatcher("/user/toLogin").forward(request, response);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				return false;
			}
		}
		return true;
	}

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		log.debug("{} time:{}", request.getRequestURI(), System.currentTimeMillis() - timeThreadLocal.get());
	}
}
