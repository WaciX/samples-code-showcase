package com.example.testJobManagement.jobs;

import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.model.Job;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;

@Getter(AccessLevel.PACKAGE)
@Builder
public class JobContext {
    private JobTrigger jobTrigger;

    public Optional<Object> getValue(String key) {
        return Optional.ofNullable(Optional.ofNullable(jobTrigger.getParameters())
                .orElse(Map.of())
                .get(key));
    }
}
