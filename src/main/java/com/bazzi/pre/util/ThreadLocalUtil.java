package com.bazzi.pre.util;

import java.util.Map;

public final class ThreadLocalUtil {
    private static final ThreadLocal<Map<String, Object>> PARAMETER_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Object> RESULT_THREAD_LOCAL = new ThreadLocal<>();

    private ThreadLocalUtil() {
    }

    public static void setParameter(Map<String, Object> parameter) {
        PARAMETER_THREAD_LOCAL.set(parameter);
    }

    public static Map<String, Object> getParameter() {
        return PARAMETER_THREAD_LOCAL.get();
    }

    public static void setResult(Object result) {
        RESULT_THREAD_LOCAL.set(result);
    }

    public static Object getResult() {
        return RESULT_THREAD_LOCAL.get();
    }

    public static void clearThreadLocal() {
        PARAMETER_THREAD_LOCAL.remove();
        RESULT_THREAD_LOCAL.remove();
    }

}
