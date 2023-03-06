package com.example.testJobManagement.entity;

import com.example.testJobManagement.model.Priority;
import com.example.testJobManagement.model.ScheduleType;
import com.example.testJobManagement.model.Status;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "job_trigger")
@TypeDef(name = "json", typeClass = JsonType.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class JobTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jobDetailId")
    @NotNull
    @Valid
    private JobDetail jobDetail;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ScheduleType scheduleType;

    // Optional, depends on scheduleType
    private String cronSchedule;

    // Optional, not all jobs have parameters to pass on
    @Type(type = "json")
    @Column(columnDefinition = "json")
    private Map<String, Object> parameters;

    @CreationTimestamp
    private Instant created;
}
