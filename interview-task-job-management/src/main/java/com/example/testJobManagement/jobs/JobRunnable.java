package com.example.testJobManagement.jobs;

// TODO this should be as part of separate package, so other projects could use the dependency directly
public interface JobRunnable {
    void run(JobContext jobContext);
}
