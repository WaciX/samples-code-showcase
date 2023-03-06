package com.example.testJobManagement.config;

import com.example.testJobManagement.jobs.JobRunnable;
import com.example.testJobManagement.services.RunnableWithPriority;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// TODO not sure if this is actually working
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = JobRunnable.class))
@Configuration
public class ExecutorsConfig {

    @Bean("priority-aware-thread-pool-executor")
    public ThreadPoolExecutor priorityAwareThreadPoolExecutor() {
        var queue = new PriorityBlockingQueue<Runnable>(100, Comparator.comparing(runnable -> {
            if (runnable instanceof RunnableWithPriority) {
                var runnableWithPriority = (RunnableWithPriority) runnable;
                switch (runnableWithPriority.getJobTrigger().getPriority()) {
                    case LOW:
                        return 2;
                    case NORMAL:
                        return 1;
                    case HIGH:
                        return 0;
                }
            }
            return 100;
        }));

        // TODO configurable number of pool size
        // TODO should also implement RejectedExecutionHandler
        return new ThreadPoolExecutor(8, 8,
                0, TimeUnit.MILLISECONDS, queue);
    }
}
