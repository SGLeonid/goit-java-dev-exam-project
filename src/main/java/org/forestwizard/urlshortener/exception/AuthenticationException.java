package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class AuthenticationException extends RuntimeException {
    private final Status status;

    public AuthenticationException(Status status) {
        super();
        this.status = status;
    }

    public AuthenticationException(Status status, Throwable cause) {
        super(cause);
        this.status = status;
    }
}
