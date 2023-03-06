package com.example.testJobManagement.utils;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.jobs.TestJobRunnable;
import com.example.testJobManagement.model.Priority;
import com.example.testJobManagement.model.ScheduleType;
import com.example.testJobManagement.model.Status;

import java.util.Map;

// TODO all "magic" strings and numbers should be part of Constraints class
public class JobDataFactory {
    public static JobDetail createJobDetail1() {
        return JobDetail.builder()
                .name("TEST1")
                .jobRunnableType(TestJobRunnable.class)
                .build();
    }

    public static JobDetail createJobDetail2() {
        return JobDetail.builder()
                .name("TEST2")
                .jobRunnableType(TestJobRunnable.class)
                .build();
    }

    public static JobTrigger createJobTrigger(JobDetail jobDetail) {
        return JobTrigger.builder()
                .jobDetail(jobDetail)
                .status(Status.QUEUED)
                .scheduleType(ScheduleType.EXECUTE_IMMEDIATE)
                .priority(Priority.NORMAL)
                .parameters(Map.of("TEST1", 1234))
                .build();
    }
}
