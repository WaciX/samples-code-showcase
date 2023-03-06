package com.example.testJobManagement.jobs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingJobRunnable implements JobRunnable {
    @Override
    public void run(JobContext jobContext) {
        log.info("Received job {}", jobContext.getJobTrigger());
    }
}
