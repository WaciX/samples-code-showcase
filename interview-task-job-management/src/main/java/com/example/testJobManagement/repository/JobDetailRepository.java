package com.example.testJobManagement.repository;

import com.example.testJobManagement.entity.JobDetail;
import com.example.testJobManagement.entity.JobTrigger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDetailRepository extends CrudRepository<JobDetail, Long> {

    List<JobDetail> findAll();

    Optional<JobDetail> findByName(String name);

}
