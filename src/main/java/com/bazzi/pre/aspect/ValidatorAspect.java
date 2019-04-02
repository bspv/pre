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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
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
    public void parameterValidator(JoinPoint joinPoint) throws Throwable {
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        // 获取方法
        Method method = AspectUtil.findMethod(joinPoint);

        // 获取方法参数名称
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        StringBuilder builder = new StringBuilder();

        //先验证普通类型的参数，并且将错误信息以参数的顺序放到argsMap中
        Map<Integer, String> argsMap = new HashMap<>();
        Set<ConstraintViolation<Object>> violations = validator.forExecutables().validateParameters(joinPoint.getThis(), method, args);
        for (ConstraintViolation<Object> violation : violations) {
            Path path = violation.getPropertyPath();// 获得校验的参数路径信息
            NodeImpl leafNode = ((PathImpl) path).getLeafNode();
            String parameterName;
            // PARAMETER，获取参数下标，然后获得参数名
            int paramIdx = leafNode.getParameterIndex();
            parameterName = paramIdx < 0 || parameterNames == null || paramIdx >= parameterNames.length ? "" : parameterNames[paramIdx];

            String message = modifyMsg(violation, parameterName);
            if (argsMap.containsKey(paramIdx)) {
                String val = argsMap.get(paramIdx);
                message = val + ";" + message;
            }
            argsMap.put(paramIdx, message);
        }

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            //普通类型，且有错误信息，直接追加
            if (argsMap.containsKey(i)) {
                builder.append(";").append(argsMap.get(i));
            } else {//普通类型，没错误信息，或者bean类型参数，进行校验
                validateForBean(arg, builder);
            }
        }

        String msg = builder.toString();

        //如果有错误信息，返回提示
        if (!"".equals(msg) && msg.startsWith(";")) {
            msg = msg.substring(1);//去掉错误提示起始位置的 `;`
            log.debug(msg);
            throw new ParameterException("-1", msg);
        }
    }

    /**
     * 以对象校验的方式来校验参数的错误信息
     *
     * @param arg     参数
     * @param builder StringBuilder
     */
    private void validateForBean(Object arg, StringBuilder builder) {
        if (arg != null) {
            Set<ConstraintViolation<Object>> validate = validator.validate(arg);
            if (validate != null && validate.size() > 0) {
                List<ConstraintViolation<Object>> list = new ArrayList<>(validate);
                Field[] fields = list.get(0).getRootBeanClass().getDeclaredFields();//获取bean的属性
                String[] errMsgArr = new String[fields.length];//属性对应的错误信息数组
                Map<String, Integer> beanMap = new HashMap<>();
                for (int j = 0; j < fields.length; j++) {
                    Field field = fields[j];
                    beanMap.put(field.getName(), j);
                }

                //将错误提示放到数组对应位置
                for (ConstraintViolation<Object> violation : list) {
                    Path path = violation.getPropertyPath();// 获得校验的参数路径信息
                    String parameterName = path.iterator().next().getName();
                    Integer idx = beanMap.get(parameterName);
                    String val = errMsgArr[idx];
                    String message = modifyMsg(violation, parameterName);
                    val = val == null || "".equals(val) ? message : val + ";" + message;
                    errMsgArr[idx] = val;
                }

                //拼接错误信息
                for (String value : errMsgArr) {
                    if (value != null && !"".equals(value))
                        builder.append(";").append(value);
                }

            }
        }
    }

    /**
     * 错误提示更改，框架自带的错误提示，则在错误信息前面加上参数名
     *
     * @param constraintViolation 约束信息
     * @param parameterName       当前参数名
     * @return 错误提示
     */
    private static String modifyMsg(ConstraintViolation<Object> constraintViolation, String parameterName) {
        String message = constraintViolation.getMessage();
        String messageTemplate = constraintViolation.getMessageTemplate();
        // 如果使用默认的模板提示，则在模板之前添加参数名
        if (Pattern.compile(MSG_T_REGULAR).matcher(messageTemplate).matches()) {
            message = "`" + parameterName + "`" + message;
        }
        return message;
    }

    /**
     * 获取参数名
     *
     * @param constraintViolation 约束信息
     * @param parameterNames      方法所有参数名
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

}
