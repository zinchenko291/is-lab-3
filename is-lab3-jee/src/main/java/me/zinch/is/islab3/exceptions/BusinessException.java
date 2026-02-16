package me.zinch.is.islab3.exceptions;

public abstract class BusinessException extends RuntimeException {
    private final Integer status;
    protected BusinessException(String message, Integer status) {
        super(message);
        this.status = status;
    }

    protected BusinessException(String message, Integer status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
