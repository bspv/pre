package com.bazzi.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 单元格配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CellAttribute {

	/**
	 * 单元格列的名称
	 *
	 * @return 列名
	 */
	String name();

	/**
	 * 顺序
	 *
	 * @return 顺序
	 */
	int order();

	/**
	 * 列宽
	 *
	 * @return 列宽
	 */
	int width() default 20;

}
