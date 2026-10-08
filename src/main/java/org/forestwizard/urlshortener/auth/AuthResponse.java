package org.forestwizard.urlshortener.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.forestwizard.urlshortener.status.Status;

@Data
@AllArgsConstructor
public class AuthResponse {
    private final Status error;
    private final String token;
}
