package com.example.testJobManagement.entity;

import com.example.testJobManagement.jobs.JobRunnable;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "job_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class JobDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    @NotNull
    @NotBlank
    private String name;

    @NotNull
    private Class<? extends JobRunnable> jobRunnableType;

    @CreationTimestamp
    private Instant created;
}
