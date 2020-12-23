package com.bazzi.pre.interceptor;

import com.bazzi.core.annotation.AllowAccess;
import com.bazzi.core.util.JsonUtil;
import com.bazzi.pre.model.User;
import com.bazzi.pre.util.ThreadLocalHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ThreadLocalHelper.setTime(System.currentTimeMillis());
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

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        Long startTime = ThreadLocalHelper.getTime();
        log.info("Completed--->URI:{}, Method:{}, Parameter:{}, SessionAttribute:{}, Result:{}, Time:{}ms",
                request.getRequestURI(),
                request.getMethod(),
                JsonUtil.toJsonString(ThreadLocalHelper.getParameter()),
                JsonUtil.toJsonString(ThreadLocalHelper.getSessionAttr()),
                JsonUtil.toJsonString(ThreadLocalHelper.getResult()),
                startTime == null ? -1 : System.currentTimeMillis() - startTime);
        ThreadLocalHelper.clearThreadLocal();
    }
}
