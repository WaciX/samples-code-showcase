package com.example.testJobManagement.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidJobException.class})
    public String invalidJobException(Exception exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({JobNotFoundException.class})
    public String jobNotFoundException(Exception exception) {
        return exception.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({JobUpdateFailedException.class})
    public String jobUpdateFailedException(Exception exception) {
        return exception.getMessage();
    }
}
