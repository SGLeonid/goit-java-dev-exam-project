package org.forestwizard.urlshortener.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    String generateToken(String username);
    boolean validateToken(String jwt, UserDetails username);
    String extractUsername(String jwt);
}