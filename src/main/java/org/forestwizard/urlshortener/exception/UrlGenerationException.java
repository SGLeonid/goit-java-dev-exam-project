package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class UrlGenerationException extends RuntimeException {
    private final Status status;

    public UrlGenerationException(Status status) {
        this.status = status;
    }
}
