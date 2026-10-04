package org.forestwizard.urlshortener.exception;

public class RedirectUrlNotFoundException extends RuntimeException {
    public RedirectUrlNotFoundException(String message) {
        super(message);
    }
}
