package com.shopflow.shopflow.exception;


/*
 * Exception métier générique
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
