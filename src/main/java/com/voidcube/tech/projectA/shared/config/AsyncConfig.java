package com.voidcube.tech.projectA.shared.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    @Bean(name = "analyticsTaskExecutor")
    public ThreadPoolTaskExecutor analyticsTaskExecutor(
        @Value("${app.async.analytics.core-pool-size:2}")
        int corePoolSize,

        @Value("${app.async.analytics.max-pool-size:4}")
        int maxPoolSize,

        @Value("${app.async.analytics.queue.capacity:500}")
        int queueCapacity,

        @Value("${app.async.analytics.await-termination-seconds:30}")
        int awaitTerminationSeconds
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("analytics-");

        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);

        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

        return executor;
    }
}
