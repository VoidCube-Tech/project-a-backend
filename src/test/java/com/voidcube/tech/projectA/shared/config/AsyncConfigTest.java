package com.voidcube.tech.projectA.shared.config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncConfigTest {

    @Test
    void deveLimitarExecutorDeAnalytics()
            throws Exception {
        AsyncConfig config = new AsyncConfig();

        ThreadPoolTaskExecutor executor =
                config.analyticsTaskExecutor(
                        1,
                        1,
                        1,
                        1
                );

        executor.initialize();

        CountDownLatch taskStarted =
                new CountDownLatch(1);

        CountDownLatch releaseTask =
                new CountDownLatch(1);

        try {
            executor.execute(() -> {
                taskStarted.countDown();

                try {
                    releaseTask.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });

            assertTrue(
                    taskStarted.await(
                            1,
                            TimeUnit.SECONDS
                    )
            );

            executor.execute(() -> {
            });
            assertThrows(
                    TaskRejectedException.class,
                    () -> executor.execute(() -> {
                    })
            );

            assertEquals(
                    1,
                    executor
                            .getThreadPoolExecutor()
                            .getCorePoolSize()
            );

            assertEquals(
                    1,
                    executor
                            .getThreadPoolExecutor()
                            .getMaximumPoolSize()
            );
        } finally {
            releaseTask.countDown();
            executor.shutdown();
        }
    }
}
