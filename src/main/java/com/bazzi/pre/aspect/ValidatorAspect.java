package com.bazzi.pre.aspect;

import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.util.AspectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
@Order(value = 1)
public class ValidatorAspect {
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private static final String MSG_T_REGULAR = "^\\{[a-zA-Z.0-9]*message\\}$";

    @Resource
    private Validator validator;

    @Before(value = "execution(public * com.bazzi.pre.controller.*.*(..))")
    public void parameterValidator(JoinPoint joinPoint) throws NoSuchMethodException {
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        // 获取方法
        Method method = AspectUtil.findMethod(joinPoint);

        // 获取方法参数名称
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        // 校验方法里bean参数
        Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();
        for (Object arg : args) {
            if (arg != null)
                constraintViolations.addAll(validator.validate(arg));
        }
        // 校验方法里普通参数
        constraintViolations.addAll(validator.forExecutables().validateParameters(joinPoint.getThis(), method, args));

        StringBuilder builder = new StringBuilder();
        for (Iterator<ConstraintViolation<Object>> iterator = constraintViolations.iterator(); iterator.hasNext(); ) {
            ConstraintViolation<Object> constraintViolation = iterator.next();
            String message = constraintViolation.getMessage();
            String messageTemplate = constraintViolation.getMessageTemplate();
            // 如果使用默认的模板提示，则在模板之前添加参数名
            if (Pattern.compile(MSG_T_REGULAR).matcher(messageTemplate).matches()) {
                message = "`" + getParameterName(constraintViolation, parameterNames) + "`" + message;
            }
            builder.append(message).append(iterator.hasNext() ? ";" : "");
        }
        if (constraintViolations.size() > 0) {
            String msg = builder.toString();
            log.debug(msg);
            throw new ParameterException("-1", msg);
        }
    }

    /**
     * 获取参数名
     *
     * @param constraintViolation 约束信息
     * @param parameterNames      方法所以参数名
     * @return 参数名
     */
    private static String getParameterName(ConstraintViolation<Object> constraintViolation, String[] parameterNames) {
        Path path = constraintViolation.getPropertyPath();// 获得校验的参数路径信息
        NodeImpl leafNode = ((PathImpl) path).getLeafNode();
        String parameterName;
        if (ElementKind.PARAMETER == leafNode.getKind()) {
            // PARAMETER，获取参数下标，然后获得参数名
            int paramIdx = leafNode.getParameterIndex();
            parameterName = paramIdx < 0 || parameterNames == null
                    || paramIdx >= parameterNames.length ? "" : parameterNames[paramIdx];
        } else {
            // 非PARAMETER，直接从路径信息中获取参数名
            parameterName = path.iterator().next().getName();
        }
        return parameterName;
    }

//	private static boolean isBaseDataType(Class clazz) throws Exception {
//		return
//				(
//						clazz.equals(String.class) ||
//								clazz.equals(Integer.class) ||
//								clazz.equals(Byte.class) ||
//								clazz.equals(Long.class) ||
//								clazz.equals(Double.class) ||
//								clazz.equals(Float.class) ||
//								clazz.equals(Character.class) ||
//								clazz.equals(Short.class) ||
//								clazz.equals(BigDecimal.class) ||
//								clazz.equals(BigInteger.class) ||
//								clazz.equals(Boolean.class) ||
//								clazz.equals(Date.class) ||
//								clazz.isPrimitive()
//				);
//	}

}
