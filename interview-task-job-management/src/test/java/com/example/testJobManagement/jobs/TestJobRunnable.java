package com.example.testJobManagement.jobs;

import org.springframework.stereotype.Component;

@Component
public class TestJobRunnable implements JobRunnable {
    @Override
    public void run(JobContext jobContext) {
        // TODO jobContext.getValue("TEST1");
    }
}
