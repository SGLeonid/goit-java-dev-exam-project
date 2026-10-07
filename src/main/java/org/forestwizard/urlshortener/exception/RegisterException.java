package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class RegisterException extends RuntimeException {
    private final Status status;

    public RegisterException(Status status, Throwable cause) {
        super(cause);
        this.status = status;
    }
}