package me.zinch.is.islab3.exceptions;

public class ConstraintException extends BusinessException {
    public ConstraintException(String message) {
        super(message, 400);
    }
}
