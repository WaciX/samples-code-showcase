package com.example.testJobManagement.model;

import com.example.testJobManagement.jobs.JobRunnable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @NotNull
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    @NotNull
    private Class<? extends JobRunnable> jobRunnableType;

    @NotNull
    private Priority priority;

    @NotNull
    private Status status;

    @NotNull
    private ScheduleType scheduleType;

    private String cronSchedule;

    private Map<String, Object> parameters;
}
