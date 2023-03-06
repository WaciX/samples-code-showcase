package com.example.testJobManagement.services;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.error.InvalidJobException;
import com.example.testJobManagement.model.Job;
import com.example.testJobManagement.model.Status;
import com.example.testJobManagement.repository.JobDetailRepository;
import com.example.testJobManagement.repository.JobTriggerRepository;
import lombok.AllArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class JobService {

    private final JobDetailRepository jobDetailRepository;
    private final JobTriggerRepository jobTriggerRepository;

    public List<JobTrigger> getAllJobTriggers() {
        return jobTriggerRepository.findAll();
    }

    public Optional<JobDetail> getJobDetailByName(String name) {
        return jobDetailRepository.findByName(name);
    }

    @Transactional
    public JobTrigger createJobDetailAndTrigger(JobDetail newJobDetail, JobTrigger jobTrigger) {
        Optional<JobDetail> jobDetail = getJobDetailByName(newJobDetail.getName());

        if (jobDetail.isEmpty()) {
            jobDetail = Optional.of(jobDetailRepository.save(newJobDetail));
        }

        if (!jobDetail.get().getJobRunnableType().equals(newJobDetail.getJobRunnableType())) {
            throw new InvalidJobException(String.format("Existing Job with name %s have different job runnable type %s than old one %s",
                    newJobDetail.getName(), newJobDetail.getJobRunnableType(), jobDetail.get().getJobRunnableType()));
        }

        if (jobTrigger.getCronSchedule() != null) {
            try {
                new CronExpression(jobTrigger.getCronSchedule());
            } catch (ParseException parseException) {
                throw new InvalidJobException(String.format("Invalid cron schedule %s. Parse error: %s at offset %s",
                        jobTrigger.getCronSchedule(), parseException.getMessage(), parseException.getErrorOffset()));
            }
        }

        jobTrigger.setJobDetail(jobDetail.get());

        return jobTriggerRepository.save(jobTrigger);
    }

    @Transactional
    public void updateStatus(Long jobTriggerId, Status status) {
        Optional<JobTrigger> jobTrigger = jobTriggerRepository.findById(jobTriggerId);

        if (jobTrigger.isPresent()) {
            jobTrigger.get().setStatus(status);
            jobTriggerRepository.save(jobTrigger.get());
        }
    }

    @Transactional
    public void deleteJobTriggerId(Long id) {
        jobTriggerRepository.deleteById(id);
    }

    public Optional<JobTrigger> getJobTriggerById(Long id) {
        return jobTriggerRepository.findById(id);
    }
}
