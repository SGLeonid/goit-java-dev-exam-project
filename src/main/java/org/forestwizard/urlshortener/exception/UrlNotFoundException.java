package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class UrlNotFoundException extends RuntimeException {
    private final Status status;

    public UrlNotFoundException(Status status) {
        super();
        this.status = status;
    }
}