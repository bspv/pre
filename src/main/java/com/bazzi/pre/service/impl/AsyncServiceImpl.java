package com.bazzi.pre.service.impl;

import com.bazzi.pre.model.User;
import com.bazzi.pre.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AsyncServiceImpl implements AsyncService {
	@Async
	public void sleepAndPrint(User user) {
		log.debug("async-method-start----------------------------------------");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.debug("async-method-end----------------------------------------"+user);
	}
}
