package com.bazzi.pre.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableAsync
public class ScheduleConfig {
	@Value("${spring.pool.size}")
	private int poolSize;
	@Value("${spring.pool.thread-name-prefix}")
	private String threadNamePrefix;
	@Value("${spring.pool.await-termination-seconds}")
	private int awaitTerminationSeconds;
	@Value("${spring.pool.wait-for-tasks-to-complete}")
	private boolean waitForTasksToComplete;

	//定时任务
	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(poolSize);
		scheduler.setThreadNamePrefix(threadNamePrefix);
		scheduler.setAwaitTerminationSeconds(awaitTerminationSeconds);
		scheduler.setWaitForTasksToCompleteOnShutdown(waitForTasksToComplete);
		return scheduler;
	}

	//线程池
//	@Bean
//	public Executor taskExecutor() {
//		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//		executor.setCorePoolSize(10);
//		executor.setMaxPoolSize(20);
//		executor.setQueueCapacity(100);
//		executor.setThreadNamePrefix("task-executor-pool-");
//		// rejection-policy:当pool已经达到max size的时候，如何处理新任务
//		// CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
//		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); //对拒绝task的处理策略
//		executor.setKeepAliveSeconds(60);
//		executor.initialize();
//		return executor;
//	}
}
