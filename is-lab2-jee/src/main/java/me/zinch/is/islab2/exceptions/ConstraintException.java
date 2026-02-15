package me.zinch.is.islab2.exceptions;

public class ConstraintException extends BusinessException {
    public ConstraintException(String message) {
        super(message, 400);
    }
}
