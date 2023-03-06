package com.example.testJobManagement.services;

import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.jobs.JobContext;
import com.example.testJobManagement.jobs.JobRunnable;
import com.example.testJobManagement.model.Status;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// TODO move it outside of services package and provide better access model
@Slf4j
@Data
@Builder
public class RunnableWithPriority implements Runnable {

    private final JobTrigger jobTrigger;
    private final JobRunnable jobRunnable;
    private final JobExecutorService jobExecutorService;

    @Override
    public void run() {
        try {
            jobExecutorService.getJobService().updateStatus(jobTrigger.getId(), Status.RUNNING);

            jobRunnable.run(JobContext.builder()
                    .jobTrigger(jobTrigger)
                    .build());

            jobExecutorService.getJobService().updateStatus(jobTrigger.getId(), Status.SUCCESS);
        } catch (Exception exception) {
            log.error("Job failed {}", jobTrigger, exception);

            jobExecutorService.getJobService().updateStatus(jobTrigger.getId(), Status.FAILED);
        } finally {
            jobExecutorService.getJobTriggerIdToRunningTask().remove(jobTrigger.getId());
        }
    }
}
