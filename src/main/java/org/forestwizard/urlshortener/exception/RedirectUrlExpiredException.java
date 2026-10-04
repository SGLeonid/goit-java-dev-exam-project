package org.forestwizard.urlshortener.exception;

public class RedirectUrlExpiredException extends RuntimeException {
    public RedirectUrlExpiredException(String message) {
        super(message);
    }
}
