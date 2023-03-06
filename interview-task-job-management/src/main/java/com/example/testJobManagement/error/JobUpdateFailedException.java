package com.example.testJobManagement.error;

public class JobUpdateFailedException extends RuntimeException {

    public JobUpdateFailedException(String msg) {
        super(msg);
    }

}
