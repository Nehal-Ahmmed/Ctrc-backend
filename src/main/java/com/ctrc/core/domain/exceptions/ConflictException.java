package com.ctrc.core.domain.exceptions;

// thrown when a request conflicts with existing state, e.g. duplicate vote
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
