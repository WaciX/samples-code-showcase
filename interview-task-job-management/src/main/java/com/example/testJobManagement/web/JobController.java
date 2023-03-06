package com.example.testJobManagement.web;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.error.JobNotFoundException;
import com.example.testJobManagement.error.JobUpdateFailedException;
import com.example.testJobManagement.mapper.JobMapper;
import com.example.testJobManagement.model.*;
import com.example.testJobManagement.services.JobExecutorService;
import com.example.testJobManagement.services.JobService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("job")
@AllArgsConstructor
public class JobController {

    private final JobExecutorService jobExecutorService;
    private final JobService jobService;
    private final JobMapper jobMapper;

    @GetMapping
    public List<Job> allJobs() {
        return jobService.getAllJobTriggers()
                .stream()
                .map(jobMapper::mapFromJobTrigger)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public Job getJobById(@PathVariable Long id) {
        return jobService.getJobTriggerById(id)
                .map(jobMapper::mapFromJobTrigger)
                .orElseThrow(() -> new JobNotFoundException(String.format("Job with id %s not found", id)));
    }

    @PostMapping("/execute-immediate")
    public Job createExecuteImmediateJob(@RequestBody CreateExecuteImmediateJobRequest request) {

        JobDetail jobDetail = JobDetail.builder()
                .name(request.getName())
                .jobRunnableType(request.getJobRunnableType())
                .build();

        JobTrigger jobTrigger = JobTrigger.builder()
                .jobDetail(jobDetail)
                .priority(request.getPriority())
                .status(Status.QUEUED)
                .scheduleType(ScheduleType.EXECUTE_IMMEDIATE)
                .cronSchedule(null)
                .parameters(request.getParameters())
                .build();

        var newJobTrigger = jobService.createJobDetailAndTrigger(jobDetail, jobTrigger);

        jobExecutorService.executeImmediate(newJobTrigger);

        return jobMapper.mapFromJobTrigger(newJobTrigger);

    }

    @PostMapping("/cron-schedule")
    public Job createCronScheduledJob(@RequestBody CreateCronScheduledJobRequest request) {

        JobDetail jobDetail = JobDetail.builder()
                .name(request.getName())
                .jobRunnableType(request.getJobRunnableType())
                .build();

        JobTrigger jobTrigger = JobTrigger.builder()
                .jobDetail(jobDetail)
                .priority(request.getPriority())
                .status(Status.QUEUED)
                .scheduleType(ScheduleType.SCHEDULED_CRON)
                .cronSchedule(request.getCronSchedule())
                .parameters(request.getParameters())
                .build();

        var newJobTrigger = jobService.createJobDetailAndTrigger(jobDetail, jobTrigger);

        jobExecutorService.cronSchedule(newJobTrigger);

        return jobMapper.mapFromJobTrigger(newJobTrigger);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        Optional<JobTrigger> jobTrigger = jobService.getJobTriggerById(id);

        if (jobTrigger.isEmpty()) {
            throw new JobNotFoundException(String.format("Job with id %s not found", id));
        }

        if (jobTrigger.get().getStatus().equals(Status.RUNNING) ) {
            throw new JobUpdateFailedException("Job already running");
        }

        if (jobTrigger.get().getStatus().equals(Status.FAILED) || jobTrigger.get().getStatus().equals(Status.SUCCESS)) {
            throw new JobUpdateFailedException("Job already finished");
        }

        jobExecutorService.cancelTask(jobTrigger.get());

        jobService.deleteJobTriggerId(id);
    }
}
