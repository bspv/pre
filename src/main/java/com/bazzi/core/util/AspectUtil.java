package com.bazzi.core.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public final class AspectUtil {
    private AspectUtil() {
    }

    /**
     * 获取当前切入点的方法
     *
     * @param joinPoint 切入点
     * @return 方法
     * @throws NoSuchMethodException 异常
     */
    public static Method findMethod(JoinPoint joinPoint) throws NoSuchMethodException {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = joinPoint.getTarget();
        return target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
    }

    /**
     * 获取当前切入点的方法，此方法获取的Method可以获取接口类中方法的注解
     *
     * @param joinPoint 切入点
     * @return 方法
     */
    public static Method findInterfaceMethod(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }

}
