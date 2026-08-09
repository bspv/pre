package com.bazzi.core.util;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring 上下文工具类，用于在非 Spring 管理的类(如静态工具类)中静态获取容器 Bean、环境配置并发布事件。
 * <p>
 * 通过实现 {@link ApplicationContextAware} 由 Spring 回调注入上下文，因此必须先注册为 Bean 才会生效。
 * 注册方式按本类是否落在组件扫描范围内任选其一：
 * <ul>
 *     <li>在扫描范围内：类上加 {@code @Component}</li>
 *     <li>不在扫描范围内：在任意配置类中用 {@code @Bean} 或 {@code @Import} 显式注册</li>
 *     <li>作为公共 jar 复用：走自动配置注册，无需使用方改扫描配置</li>
 * </ul>
 * 多种方式不要叠加使用，Bean 名相同会冲突，不同则会产生多个实例。
 */
public final class SpringContextUtil implements ApplicationContextAware {

    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        SpringContextUtil.applicationContext = applicationContext;
    }

    /**
     * 按类型获取 Bean
     *
     * @param clazz Bean 类型
     * @param <T>   类型
     * @return Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 按名称获取 Bean
     *
     * @param name Bean 名称
     * @return Bean 实例
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 按名称和类型获取 Bean
     *
     * @param name  Bean 名称
     * @param clazz Bean 类型
     * @param <T>   类型
     * @return Bean 实例
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 是否包含指定名称的 Bean
     *
     * @param name Bean 名称
     * @return 是否包含
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 获取当前激活的 profiles
     *
     * @return 激活的 profiles
     */
    public static String[] getActiveProfiles() {
        return applicationContext.getEnvironment().getActiveProfiles();
    }

    /**
     * 读取配置项
     *
     * @param key 配置 key
     * @return 配置值，不存在返回 null
     */
    public static String getProperty(String key) {
        return applicationContext.getEnvironment().getProperty(key);
    }

    /**
     * 发布应用事件
     *
     * @param event 事件
     */
    public static void publishEvent(Object event) {
        applicationContext.publishEvent(event);
    }

}
