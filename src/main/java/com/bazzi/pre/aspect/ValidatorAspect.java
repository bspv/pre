package com.bazzi.pre.aspect;

import com.bazzi.core.annotation.GroupCfg;
import com.bazzi.core.annotation.ValidCfg;
import com.bazzi.core.ex.ParameterException;
import com.bazzi.core.generic.TipsCodeEnum;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Slf4j
@Aspect
@Component
@Order(value = 1)
public class ValidatorAspect {
    private static final ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    private static final String MSG_T_REGULAR = "^\\{[a-zA-Z.0-9]*message}$";

    @Resource
    private Validator validator;

    @Before(value = "execution(public * com.bazzi.pre.controller.*.*(..)) " +
            "|| execution(public * com.bazzi.pre.controller.*.*.*(..))")
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
        if (msg.startsWith(";")) {
            msg = msg.substring(1);//去掉错误提示起始位置的 `;`
            log.debug(msg);
            throw new ParameterException(TipsCodeEnum.CODE_0002.getCode(), msg);
        }
    }

    /**
     * 以对象校验的方式来校验参数的错误信息
     *
     * @param arg     参数
     * @param builder StringBuilder
     */
    private void validateForBean(Object arg, StringBuilder builder) {
        if (arg == null) return;

        Class<?>[] groupClass = getGroupClass(arg);
        Set<ConstraintViolation<Object>> validate = getViolations(arg, groupClass);
        if (CollectionUtils.isEmpty(validate)) return;

        List<ConstraintViolation<Object>> list = new ArrayList<>(validate);
        Field[] fields = getAllDeclaredFields(list.get(0));
        String[] errMsgArr = new String[fields.length];
        Map<String, Integer> beanMap = createFieldIndexMap(fields);

        populateErrorMessages(list, beanMap, errMsgArr);
        appendErrorMessages(builder, errMsgArr);
    }

    // 获取校验结果
    private Set<ConstraintViolation<Object>> getViolations(Object arg, Class<?>[] groupClass) {
        return (groupClass == null || groupClass.length == 0) ?
                validator.validate(arg) :
                validator.validate(arg, groupClass);
    }

    // 获取所有字段（包括父类）
    private Field[] getAllDeclaredFields(ConstraintViolation<?> violation) {
        Class<?> rootClass = violation.getRootBeanClass();
        Class<?> superClass = rootClass.getSuperclass();

        if (superClass == Object.class) {
            return rootClass.getDeclaredFields();
        } else {
            return Stream.concat(
                    Arrays.stream(superClass.getDeclaredFields()),
                    Arrays.stream(rootClass.getDeclaredFields())
            ).toArray(Field[]::new);
        }
    }

    // 创建字段名到索引的映射
    private Map<String, Integer> createFieldIndexMap(Field[] fields) {
        Map<String, Integer> beanMap = new HashMap<>();
        for (int j = 0; j < fields.length; j++) {
            beanMap.put(fields[j].getName(), j);
        }
        return beanMap;
    }

    // 填充错误信息数组
    private void populateErrorMessages(
            List<ConstraintViolation<Object>> list,
            Map<String, Integer> beanMap,
            String[] errMsgArr) {
        for (ConstraintViolation<Object> violation : list) {
            String parameterName = getParameterNameFromViolation(violation);
            Integer idx = beanMap.get(parameterName);
            if (idx == null) continue;

            String message = modifyMsg(violation, parameterName);
            errMsgArr[idx] = (errMsgArr[idx] == null) ?
                    message : errMsgArr[idx] + ";" + message;
        }
    }

    // 从校验结果中获取参数名
    private String getParameterNameFromViolation(ConstraintViolation<Object> violation) {
        return violation.getPropertyPath()
                .iterator()
                .next()
                .getName();
    }

    // 拼接最终错误信息
    private void appendErrorMessages(StringBuilder builder, String[] errMsgArr) {
        Arrays.stream(errMsgArr)
                .filter(Objects::nonNull)
                .filter(msg -> !msg.isEmpty())
                .forEach(msg -> builder.append(";").append(msg));
    }

    /**
     * 判断arg是否有ValidCfg注解，如果有的话，获取对应的分组信息
     *
     * @param arg 参数
     * @return 分组信息
     */
    private static Class<?>[] getGroupClass(Object arg) {
        Class<?> argClass = arg.getClass();
        if (!argClass.isAnnotationPresent(ValidCfg.class))
            return new Class[0];
        ValidCfg validCfg = argClass.getDeclaredAnnotation(ValidCfg.class);
        String fieldName = Objects.requireNonNull(validCfg).field();
        Object fieldValue = getValueByField(fieldName, arg);
        String value = fieldValue == null ? "" : String.valueOf(fieldValue);

        for (GroupCfg groupCfg : validCfg.groupCfg()) {
            if (value.equals(groupCfg.val())) {
                return groupCfg.groups();
            }
        }
        return new Class[0];
    }

    /**
     * 获取属性的值
     *
     * @param fieldName 属性名
     * @param t         对象
     * @param <T>       对象类型
     * @return 属性值
     */
    public static <T> Object getValueByField(String fieldName, T t) {
        if (fieldName == null || fieldName.isEmpty() || t == null)
            return null;
        try {
            return getValue(fieldName, t.getClass().getDeclaredField(fieldName), t);
        } catch (NoSuchFieldException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取属性的值
     *
     * @param fieldName 属性名
     * @param field     属性
     * @param t         对象
     * @param <T>       对象类型
     * @return 属性值
     */
    private static <T> Object getValue(String fieldName, Field field, T t) {
        Object value = null;
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            boolean isBoolean = "boolean".equalsIgnoreCase(
                    field.getType().getSimpleName());
            String getMethodName = (isBoolean ? "is" : "get") + firstLetter
                    + fieldName.substring(1);
            value = t.getClass().getMethod(getMethodName).invoke(t);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return value;
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
    public static String getParameterName(ConstraintViolation<Object> constraintViolation, String[] parameterNames) {
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
