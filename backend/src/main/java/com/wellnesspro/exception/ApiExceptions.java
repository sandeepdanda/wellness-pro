package com.wellnesspro.exception;

/** Domain exceptions mapped to HTTP status codes by GlobalExceptionHandler. */
public final class ApiExceptions {

    private ApiExceptions() {}

    /** 404 - requested entity does not exist. */
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) { super(message); }
    }

    /** 409 - request conflicts with current state (duplicate email, full class, double booking). */
    public static class ConflictException extends RuntimeException {
        public ConflictException(String message) { super(message); }
    }

    /** 400 - request is semantically invalid beyond bean-validation. */
    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) { super(message); }
    }
}
