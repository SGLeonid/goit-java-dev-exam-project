package org.forestwizard.urlshortener.auth;

import lombok.Data;

@Data
public class AuthRequest {
    private final String username;
    private final String password;
}
