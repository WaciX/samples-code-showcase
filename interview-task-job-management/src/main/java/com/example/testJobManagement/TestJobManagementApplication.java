package com.example.testJobManagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TestJobManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestJobManagementApplication.class, args);
	}

}
