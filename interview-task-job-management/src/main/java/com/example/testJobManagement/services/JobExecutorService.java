package com.example.testJobManagement.services;

import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.error.InvalidJobException;
import com.example.testJobManagement.jobs.JobRunnable;
import lombok.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class JobExecutorService {

    @Getter(AccessLevel.PACKAGE)
    private final JobService jobService;
    private final List<JobRunnable> jobRunnables;
    private final TaskScheduler taskScheduler;
    private final ThreadPoolExecutor threadPoolExecutor;
    @Getter(AccessLevel.PACKAGE)
    private final Map<Long, Future<?>> jobTriggerIdToRunningTask = new ConcurrentHashMap<>();

    public JobExecutorService(JobService jobService, List<JobRunnable> jobRunnables,
                              TaskScheduler taskScheduler,
                              @Qualifier("priority-aware-thread-pool-executor") ThreadPoolExecutor threadPoolExecutor) {
        this.jobService = jobService;
        this.jobRunnables = jobRunnables;
        this.taskScheduler = taskScheduler;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void executeImmediate(JobTrigger jobTrigger) {
        // TODO this whole section should be guarded by lock on that jobTrigger

        if (jobTriggerIdToRunningTask.containsKey(jobTrigger.getId())) {
            // TODO what should happen here ?
            return;
        }

        Runnable runnable = RunnableWithPriority.builder()
                .jobExecutorService(this)
                .jobTrigger(jobTrigger)
                .jobRunnable(getJobRunnableByType(jobTrigger.getJobDetail().getJobRunnableType()))
                .build();

        Future<?> future = threadPoolExecutor.submit(runnable);

        // TODO technically the same trigger can fire while other is running due to a race condition. Lock here ?
        jobTriggerIdToRunningTask.put(jobTrigger.getId(), future);
    }

    public void cronSchedule(JobTrigger jobTrigger) {
        Future<?> future = taskScheduler.schedule(() -> executeImmediate(jobTrigger),
                new CronTrigger(jobTrigger.getCronSchedule()));

        // TODO technically the same trigger can fire while other is running.
        jobTriggerIdToRunningTask.put(jobTrigger.getId(), future);
    }

    public void cancelTask(JobTrigger jobTrigger) {
        Optional.ofNullable(jobTriggerIdToRunningTask.remove(jobTrigger.getId()))
                .ifPresent(future -> future.cancel(false));
    }

    private JobRunnable getJobRunnableByType(Class<? extends JobRunnable> type) {
        // TODO cache ?
        return jobRunnables.stream()
                .filter(jobRunnable -> jobRunnable.getClass().equals(type))
                .findFirst()
                .orElseThrow(() -> new InvalidJobException(String.format("Job with type %s not found. Make sure it's a discoverable bean by Spring", type)));
    }

}
