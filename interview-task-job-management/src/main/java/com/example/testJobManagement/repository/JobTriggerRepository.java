package com.example.testJobManagement.repository;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobTriggerRepository extends CrudRepository<JobTrigger, Long> {

    List<JobTrigger> findAll();
}
