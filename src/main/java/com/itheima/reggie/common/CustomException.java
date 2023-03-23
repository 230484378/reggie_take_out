package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j

public class CustomException extends RuntimeException {
    public CustomException(String message){
        super(message);
    }

}