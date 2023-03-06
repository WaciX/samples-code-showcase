package com.example.testJobManagement.error;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String msg) {
        super(msg);
    }

}
