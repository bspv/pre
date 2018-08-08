package com.bazzi.pre.aspect;

import com.bazzi.core.ex.ParameterException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
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

@Slf4j
@Aspect
@Component
@Order(value = 1)
public class ValidatorAspect {
	private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();

	@Resource
	private Validator validator;

	@Before(value = "execution(public * com.bazzi.pre.controller.*.*(..))")
	public void parameterValidator(JoinPoint joinPoint) {
		// 获取请求参数
		Object[] args = joinPoint.getArgs();
		// 获取方法
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Method method = methodSignature.getMethod();

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
			String message = constraintViolation.getMessage();
			// 非自定义message，则添加参数名，否则不变
			message = (message.contains(parameterName) ? "" : parameterName) + message;
//			message = (message.contains(parameterName) ? "" : "`" + parameterName + "`") + message;
			builder.append(message).append(iterator.hasNext() ? ";" : "");
		}
		if (constraintViolations.size() > 0) {
			String msg = builder.toString();
			log.debug(msg);
			throw new ParameterException("-1", msg);
		}
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
