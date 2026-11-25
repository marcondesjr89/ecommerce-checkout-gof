package com.dio.gof.platform.api;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
