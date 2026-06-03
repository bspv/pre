package com.bazzi.pre.interceptor;

import com.bazzi.core.util.JsonUtil;
import com.bazzi.pre.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class CustomInterceptor implements HandlerInterceptor {

    private final NamedThreadLocal<Long> timeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        timeThreadLocal.set(System.currentTimeMillis());
        return true;
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.info("Completed--->URI:{}, Method:{},Parameter:{}, Result:{}, Time:{}ms",
                request.getRequestURI(), request.getMethod(), JsonUtil.toJsonString(ThreadLocalUtil.getParameter()),
                JsonUtil.toJsonString(ThreadLocalUtil.getResult()),
                System.currentTimeMillis() - timeThreadLocal.get());
        ThreadLocalUtil.clearThreadLocal();
    }

}
