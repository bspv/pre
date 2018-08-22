package com.bazzi.pre.aspect;

import com.bazzi.core.util.AspectUtil;
import com.bazzi.core.util.JsonUtil;
import com.bazzi.pre.util.ThreadLocalHelper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@Order(value = 0)
public class ParamAspect {

    @Resource
    private HttpServletRequest request;

    @Before(value = "execution(public * com.bazzi.pre.controller.*.*(..))")
    public void beforeHandler(JoinPoint joinPoint) throws Throwable {
        Method currentMethod = AspectUtil.findMethod(joinPoint);
        Annotation[][] parameterAnnotations = currentMethod.getParameterAnnotations();
        // 获取请求参数
        Map<String, Object> paramMap = new HashMap<>();
        if (hasRequestBody(parameterAnnotations)) {
            String bodyData = "{}";
            Parameter[] parameters = currentMethod.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                if (parameter.isAnnotationPresent(RequestBody.class)) {
                    bodyData = JsonUtil.toJsonString(joinPoint.getArgs()[i]);
                    break;
                }
            }
            paramMap.putAll(JsonUtil.parseMap(bodyData, String.class, Object.class));
        } else {
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                String parameterVal = request.getParameter(parameterName);
                paramMap.put(parameterName, parameterVal);
            }
        }
        ThreadLocalHelper.setParameter(paramMap);

        Map<String, Object> sessionAttr = new HashMap<>();
        // 获取session中的用户等信息
        Enumeration<String> attributeNames = request.getSession().getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            Object obj = request.getSession().getAttribute(name);
            sessionAttr.put(name, obj);
        }
        ThreadLocalHelper.setSessionAttr(sessionAttr);
    }

    @AfterReturning(pointcut = "execution(public * com.bazzi.pre.controller.*.*(..)) " +
            "|| execution(public * com.bazzi.pre.handler.PreExceptionHandler.*(..))", returning = "rvt")
    public Object afterHandlerReturning(JoinPoint joinPoint, Object rvt) {
        ThreadLocalHelper.setResult(rvt);
        return rvt;
    }

    /**
     * 是否有@RequestBody注解
     *
     * @param parameterAnnotations 所有参数的注解
     * @return 有返回true，否则false
     */
    private static boolean hasRequestBody(Annotation[][] parameterAnnotations) {
        if (parameterAnnotations == null)
            return false;
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            for (Annotation annotation : parameterAnnotation) {
                if (RequestBody.class.equals(annotation.annotationType()))
                    return true;
            }
        }
        return false;
    }

}
