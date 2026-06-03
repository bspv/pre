package com.bazzi.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface GroupCfg {

    /**
     * 值
     *
     * @return 值
     */
    String val();

    /**
     * 校验分组
     *
     * @return 校验分组
     */
    Class<?>[] groups();

}
