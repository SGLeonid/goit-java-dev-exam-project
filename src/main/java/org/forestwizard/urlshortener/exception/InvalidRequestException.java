package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class InvalidRequestException extends RuntimeException {
    private final Status status;

    public InvalidRequestException(Status status) {
        this.status = status;
    }
}
