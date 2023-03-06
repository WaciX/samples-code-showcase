package com.example.testJobManagement.mapper;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.model.Job;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class JobMapper {

    public Job mapFromJobTrigger(JobTrigger jobTrigger) {
        Objects.requireNonNull(jobTrigger);
        return Job.builder()
                .id(jobTrigger.getId())
                .name(jobTrigger.getJobDetail().getName())
                .jobRunnableType(jobTrigger.getJobDetail().getJobRunnableType())
                .priority(jobTrigger.getPriority())
                .status(jobTrigger.getStatus())
                .scheduleType(jobTrigger.getScheduleType())
                .cronSchedule(jobTrigger.getCronSchedule())
                .parameters(jobTrigger.getParameters())
                .build();
    }

}
