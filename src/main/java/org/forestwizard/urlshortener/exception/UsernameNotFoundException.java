package org.forestwizard.urlshortener.exception;

import lombok.Getter;
import org.forestwizard.urlshortener.status.Status;

@Getter
public class UsernameNotFoundException extends RuntimeException {
    private final Status status;

    public UsernameNotFoundException(Status status) {
        super();
        this.status = status;
    }
}
