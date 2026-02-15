package me.zinch.is.islab2.exceptions;

public abstract class BusinessException extends RuntimeException {
    private final Integer status;
    protected BusinessException(String message, Integer status) {
        super(message);
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
