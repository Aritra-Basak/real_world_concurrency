package com.demon.concurrencyPoc.exceptionHandling.customExceptions;

public class ConcurrentUpdateException extends RuntimeException {
    public ConcurrentUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
