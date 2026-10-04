package org.forestwizard.urlshortener.auth;

import org.forestwizard.urlshortener.exception.InvalidRequestException;

public interface IAuthService {
    AuthResponse register(AuthRequest request) throws InvalidRequestException;
    AuthResponse login(AuthRequest request) throws InvalidRequestException;
}

