package com.bazzi.pre.util;

import org.springframework.core.NamedThreadLocal;

import java.util.Map;

public final class ThreadLocalHelper {
    // 计时
    private static final NamedThreadLocal<Long> timeThreadLocal = new NamedThreadLocal<>("StopWatch-StartTime");
    // 请求参数
    private static final ThreadLocal<Map<String, Object>> parameterThreadLocal = new ThreadLocal<>();
    // 会话属性
    private static final ThreadLocal<Map<String, Object>> sessionThreadLocal = new ThreadLocal<>();
    // 请求结果
    private static final ThreadLocal<Object> resultThreadLocal = new ThreadLocal<>();

    public static void setTime(Long time) {
        timeThreadLocal.set(time);
    }

    public static Long getTime() {
        return timeThreadLocal.get();
    }

    public static void setParameter(Map<String, Object> parameter) {
        parameterThreadLocal.set(parameter);
    }

    public static Map<String, Object> getParameter() {
        return parameterThreadLocal.get();
    }

    public static void setSessionAttr(Map<String, Object> sessionAttr) {
        sessionThreadLocal.set(sessionAttr);
    }

    public static Map<String, Object> getSessionAttr() {
        return sessionThreadLocal.get();
    }

    public static void setResult(Object result) {
        resultThreadLocal.set(result);
    }

    public static Object getResult() {
        return resultThreadLocal.get();
    }

    public static void clearThreadLocal() {
        setTime(null);
        setParameter(null);
        setSessionAttr(null);
        setResult(null);
    }
}
