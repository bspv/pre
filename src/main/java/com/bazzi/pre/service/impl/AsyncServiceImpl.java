package com.bazzi.pre.service.impl;

import com.bazzi.pre.model.User;
import com.bazzi.pre.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AsyncServiceImpl implements AsyncService {
    @Async
//	@EventListener(condition = "#user.userName==@propertyUtil.getProperty('admin.name') && #user.mobile==@propertyUtil.getProperty('admin.mobile')")//propertyUtil是读取配置文件的工具类，需在spring容器中
//	@EventListener(condition = "#user.userName==T(com.bazzi.pre.util.Constant).ADMIN_NAME && #user.mobile=='15311111111'")
//	@EventListener(condition = "#user.userName=='admin' && #user.mobile=='15311111111'")
    public void sleepAndPrint(User user) {
        log.debug("async-method-start----------------------------------------");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        log.debug("async-method-end----------------------------------------{}", user);
    }
}
