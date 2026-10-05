package org.forestwizard.urlshortener.auth;

import lombok.RequiredArgsConstructor;
import org.forestwizard.urlshortener.exception.AuthenticationException;
import org.forestwizard.urlshortener.exception.InvalidRequestException;
import org.forestwizard.urlshortener.exception.RegisterException;
import org.forestwizard.urlshortener.security.JwtService;
import org.forestwizard.urlshortener.status.Status;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_USERNAME_LENGTH = 255;
    private static final int MAX_PASSWORD_LENGTH = 255;

    private final AuthUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(AuthRequest request) throws InvalidRequestException, RegisterException {
        validateAuthRequest(request);
        if (userDetailsService.hasUser(request.getUsername())) {
            throw new RegisterException(Status.SUCH_USER_ALREADY_EXISTS);
        }

        userDetailsService.saveUser(AuthUser.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build()
        );

        String jwt = jwtService.generateToken(request.getUsername());
        return new AuthResponse(Status.OK, jwt);
    }

    @Override
    public AuthResponse login(AuthRequest request) throws InvalidRequestException, AuthenticationException {
        validateAuthRequest(request);
        UserDetails user;

        try {
            user = userDetailsService.loadUserByUsername(request.getUsername());
        } catch (UsernameNotFoundException e) {
            throw new AuthenticationException(Status.INVALID_USERNAME_OR_PASSWORD, e);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthenticationException(Status.INVALID_USERNAME_OR_PASSWORD);
        }

        String jwt = jwtService.generateToken(request.getUsername());
        return new AuthResponse(Status.OK, jwt);
    }

    private void validateAuthRequest(AuthRequest request) throws InvalidRequestException {
        if (request == null) {
            throw new InvalidRequestException(Status.REQUEST_BODY_BAD_OR_MISSING);
        }

        if (request.getUsername() == null) {
            throw new InvalidRequestException(Status.USERNAME_CANNOT_BE_NULL);
        }
        if (request.getUsername().trim().isEmpty() || request.getUsername().length() > MAX_USERNAME_LENGTH) {
            throw new InvalidRequestException(Status.INVALID_USERNAME_LENGTH);
        }

        if (request.getPassword() == null) {
            throw new InvalidRequestException(Status.PASSWORD_CANNOT_BE_NULL);
        }
        if (request.getPassword().trim().isEmpty()
                || request.getPassword().length() > MAX_PASSWORD_LENGTH
                || request.getPassword().length() < MIN_PASSWORD_LENGTH
        ) {
            throw new InvalidRequestException(Status.INVALID_PASSWORD_LENGTH);
        }
        if (!(request.getPassword().chars().anyMatch(Character::isUpperCase)
                && request.getPassword().chars().anyMatch(Character::isLowerCase)
                && request.getPassword().chars().anyMatch(Character::isDigit)
        )) {
            throw new InvalidRequestException(Status.PASSWORD_REQUIREMENTS_UNSATISFIED);
        }
    }
}