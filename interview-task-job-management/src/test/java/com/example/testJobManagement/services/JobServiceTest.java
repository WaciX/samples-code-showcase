package com.example.testJobManagement.services;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import com.example.testJobManagement.error.InvalidJobException;
import com.example.testJobManagement.jobs.TestJobRunnable;
import com.example.testJobManagement.jobs.TestJobRunnable2;
import com.example.testJobManagement.model.Priority;
import com.example.testJobManagement.model.ScheduleType;
import com.example.testJobManagement.model.Status;
import com.example.testJobManagement.repository.JobDetailRepository;
import com.example.testJobManagement.repository.JobTriggerRepository;
import com.example.testJobManagement.utils.JobDataFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class})
@SpringBootTest
class JobServiceTest {

    @Autowired
    JobDetailRepository jobDetailRepository;

    @Autowired
    JobTriggerRepository jobTriggerRepository;

    @Autowired
    JobService jobService;

    @Autowired
    EntityManager entityManager;

    // TODO there is probably a better way to do this
    @Transactional
    void clear() {
        entityManager.createQuery("delete from JobTrigger").executeUpdate();
        entityManager.createQuery("delete from JobDetail").executeUpdate();
    }

    @Test
    @Transactional
    void twoJobTriggers_getAllJobTriggers_twoReturned() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobDetail jobDetail2 = jobDetailRepository.save(JobDataFactory.createJobDetail2());
        JobTrigger jobTrigger1 = jobTriggerRepository.save(JobDataFactory.createJobTrigger(jobDetail1));
        JobTrigger jobTrigger2 = jobTriggerRepository.save(JobDataFactory.createJobTrigger(jobDetail2));

        List<JobTrigger> jobTriggers = jobService.getAllJobTriggers();

        assertThat(jobTriggers)
                .hasSize(2)
                .contains(jobTrigger1, jobTrigger2);
    }

    @Test
    @Transactional
    void twoJobDetails_getJobDetailByName_oneReturned() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobDetail jobDetail2 = jobDetailRepository.save(JobDataFactory.createJobDetail2());

        Optional<JobDetail> jobDetail = jobService.getJobDetailByName("TEST1");

        assertThat(jobDetail)
                .isNotEmpty()
                .contains(jobDetail1);
    }

    @Test
    @Transactional
    void noJobDetailExist_createJobDetailAndTrigger_jobDetailsAndTriggerCreated() {
        clear();
        JobDetail jobDetail1 = JobDataFactory.createJobDetail1();
        JobTrigger jobTrigger1 = JobDataFactory.createJobTrigger(jobDetail1);

        JobTrigger jobTrigger = jobService.createJobDetailAndTrigger(jobDetail1, jobTrigger1);

        assertThat(jobTrigger).isNotNull();
        assertThat(jobTrigger.getCronSchedule()).isNull();
        assertThat(jobTrigger.getStatus()).isEqualTo(Status.QUEUED);
        assertThat(jobTrigger.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(jobTrigger.getScheduleType()).isEqualTo(ScheduleType.EXECUTE_IMMEDIATE);
        assertThat(jobTrigger.getParameters()).hasSize(1)
                .containsEntry("TEST1", 1234);
        assertThat(jobTrigger.getJobDetail()).isNotNull();
        assertThat(jobTrigger.getJobDetail().getName()).isEqualTo("TEST1");
        assertThat(jobTrigger.getJobDetail().getJobRunnableType()).isEqualTo(TestJobRunnable.class);
    }

    @Test
    @Transactional
    void jobDetailExist_createJobDetailAndTrigger_jobTriggerCreated() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        jobDetail1 = jobDetail1.toBuilder().build();
        JobTrigger jobTrigger1 = JobDataFactory.createJobTrigger(jobDetail1);

        JobTrigger jobTrigger = jobService.createJobDetailAndTrigger(jobDetail1, jobTrigger1);

        assertThat(jobTrigger).isNotNull();
        assertThat(jobTrigger.getCronSchedule()).isNull();
        assertThat(jobTrigger.getStatus()).isEqualTo(Status.QUEUED);
        assertThat(jobTrigger.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(jobTrigger.getScheduleType()).isEqualTo(ScheduleType.EXECUTE_IMMEDIATE);
        assertThat(jobTrigger.getParameters()).hasSize(1)
                .containsEntry("TEST1", 1234);
        assertThat(jobTrigger.getJobDetail()).isNotNull();
        assertThat(jobTrigger.getJobDetail().getName()).isEqualTo("TEST1");
        assertThat(jobTrigger.getJobDetail().getJobRunnableType()).isEqualTo(TestJobRunnable.class);
    }

    @Test
    @Transactional
    void jobDetailExistButWithDifferentDetails_createJobDetailAndTrigger_exception() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        var jobDetail1New = jobDetail1.toBuilder()
                .jobRunnableType(TestJobRunnable2.class)
                .build();
        JobTrigger jobTrigger1 = JobDataFactory.createJobTrigger(jobDetail1New);

        assertThatThrownBy(() -> jobService.createJobDetailAndTrigger(jobDetail1New, jobTrigger1))
                .isInstanceOf(InvalidJobException.class);
    }

    @Test
    @Transactional
    void jobTriggerWithInvalidCronScheduleSyntax_createJobDetailAndTrigger_exception() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobTrigger jobTrigger1 = JobDataFactory.createJobTrigger(jobDetail1);
        jobTrigger1.setScheduleType(ScheduleType.SCHEDULED_CRON);
        jobTrigger1.setCronSchedule("INVALID");

        assertThatThrownBy(() -> jobService.createJobDetailAndTrigger(jobDetail1, jobTrigger1))
                .isInstanceOf(InvalidJobException.class);
    }

    @Test
    @Transactional
    void jobTriggerWithCronSchedule_createJobDetailAndTrigger_jobTriggerCreated() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobTrigger jobTrigger1 = JobDataFactory.createJobTrigger(jobDetail1);
        jobTrigger1.setScheduleType(ScheduleType.SCHEDULED_CRON);
        jobTrigger1.setCronSchedule("0 * * * * ?");

        JobTrigger jobTrigger = jobService.createJobDetailAndTrigger(jobDetail1, jobTrigger1);

        assertThat(jobTrigger).isNotNull();
        assertThat(jobTrigger.getCronSchedule()).isEqualTo("0 * * * * ?");
        assertThat(jobTrigger.getStatus()).isEqualTo(Status.QUEUED);
        assertThat(jobTrigger.getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(jobTrigger.getScheduleType()).isEqualTo(ScheduleType.SCHEDULED_CRON);
        assertThat(jobTrigger.getParameters()).hasSize(1)
                .containsEntry("TEST1", 1234);
        assertThat(jobTrigger.getJobDetail()).isNotNull();
        assertThat(jobTrigger.getJobDetail().getName()).isEqualTo("TEST1");
        assertThat(jobTrigger.getJobDetail().getJobRunnableType()).isEqualTo(TestJobRunnable.class);
    }

    @Test
    @Transactional
    void jobTriggerExisting_updateStatus_statusUpdated() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobTrigger jobTrigger1 = jobTriggerRepository.save(JobDataFactory.createJobTrigger(jobDetail1));

        jobService.updateStatus(jobTrigger1.getId(), Status.SUCCESS);

        Optional<JobTrigger> jobTrigger = jobTriggerRepository.findById(jobTrigger1.getId());
        assertThat(jobTrigger).isNotEmpty();
        assertThat(jobTrigger.get().getCronSchedule()).isNull();
        assertThat(jobTrigger.get().getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(jobTrigger.get().getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(jobTrigger.get().getScheduleType()).isEqualTo(ScheduleType.EXECUTE_IMMEDIATE);
        assertThat(jobTrigger.get().getParameters()).hasSize(1)
                .containsEntry("TEST1", 1234);
        assertThat(jobTrigger.get().getJobDetail()).isNotNull();
        assertThat(jobTrigger.get().getJobDetail().getName()).isEqualTo("TEST1");
        assertThat(jobTrigger.get().getJobDetail().getJobRunnableType()).isEqualTo(TestJobRunnable.class);
    }

    // TODO jobTriggerDoesNotExist_updateStatus_nothingHappens

    @Test
    @Transactional
    void jobTriggerExisting_getJobTriggerById_returnsJobTrigger() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobTrigger jobTrigger1 = jobTriggerRepository.save(JobDataFactory.createJobTrigger(jobDetail1));

        Optional<JobTrigger> jobTrigger = jobService.getJobTriggerById(jobTrigger1.getId());
        assertThat(jobTrigger).isNotEmpty();
        assertThat(jobTrigger.get().getCronSchedule()).isNull();
        assertThat(jobTrigger.get().getStatus()).isEqualTo(Status.QUEUED);
        assertThat(jobTrigger.get().getPriority()).isEqualTo(Priority.NORMAL);
        assertThat(jobTrigger.get().getScheduleType()).isEqualTo(ScheduleType.EXECUTE_IMMEDIATE);
        assertThat(jobTrigger.get().getParameters()).hasSize(1)
                .containsEntry("TEST1", 1234);
        assertThat(jobTrigger.get().getJobDetail()).isNotNull();
        assertThat(jobTrigger.get().getJobDetail().getName()).isEqualTo("TEST1");
        assertThat(jobTrigger.get().getJobDetail().getJobRunnableType()).isEqualTo(TestJobRunnable.class);
    }

    // TODO jobTriggerDoesNotExist_getJobTriggerById_emptyResult

    @Test
    @Transactional
    void jobTriggerExisting_deleteJobTriggerId_jobTriggerDeleted() {
        clear();
        JobDetail jobDetail1 = jobDetailRepository.save(JobDataFactory.createJobDetail1());
        JobTrigger jobTrigger1 = jobTriggerRepository.save(JobDataFactory.createJobTrigger(jobDetail1));

        jobService.deleteJobTriggerId(jobTrigger1.getId());

        Optional<JobTrigger> jobTrigger = jobTriggerRepository.findById(jobTrigger1.getId());
        assertThat(jobTrigger).isEmpty();
        Optional<JobDetail> jobDetail = jobDetailRepository.findById(jobDetail1.getId());
        assertThat(jobDetail).isNotEmpty();
    }
}