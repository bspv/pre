package com.bazzi.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValidCfg {

    /**
     * 需要校验的字段名
     *
     * @return 字段名
     */
    String field();

    /**
     * field具体值对应的分组配置
     *
     * @return 分组配置
     */
    GroupCfg[] groupCfg() default @GroupCfg(val = "", groups = {String.class});

}
