package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.*;
import org.forestwizard.urlshortener.exception.AuthenticationException;
import org.forestwizard.urlshortener.exception.InvalidRequestException;
import org.forestwizard.urlshortener.exception.RegisterException;
import org.forestwizard.urlshortener.security.JwtService;
import org.forestwizard.urlshortener.status.Status;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthServiceTest {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private AuthUserDetailsService authUserDetailsService;
    @Autowired
    private JwtService jwtService;
    private static AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authUserDetailsService, new BCryptPasswordEncoder(), jwtService);
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegistersUser() {
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1234");

        AuthResponse response = assertDoesNotThrow(() -> authService.register(request));
        assertNotNull(response);

        UserDetails userDetails = assertDoesNotThrow(() -> authUserDetailsService.loadUserByUsername("ForestWizard"));
        assertNotNull(userDetails);

        assertEquals(Status.OK, response.getError());
        assertFalse(response.getToken().isEmpty());
        assertEquals("ForestWizard", jwtService.extractUsername(response.getToken()));
        assertTrue(jwtService.validateToken(response.getToken(), userDetails));
    }

    @Test
    void testThrowsOnAlreadyExistingUser() {
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1234");
        authService.register(request);
        RegisterException exception = assertThrows(RegisterException.class, () -> authService.register(request));
        assertEquals(Status.SUCH_USER_ALREADY_EXISTS, exception.getStatus());
    }

    @Test
    void testAuthenticatesUser() {
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1234");
        assertDoesNotThrow(() -> authService.register(request));

        AuthResponse response = assertDoesNotThrow(() -> authService.login(request));
        assertNotNull(response);

        UserDetails userDetails = assertDoesNotThrow(() -> authUserDetailsService.loadUserByUsername("ForestWizard"));
        assertNotNull(userDetails);

        assertEquals(Status.OK, response.getError());
        assertFalse(response.getToken().isEmpty());
        assertEquals("ForestWizard", jwtService.extractUsername(response.getToken()));
        assertTrue(jwtService.validateToken(response.getToken(), userDetails));
    }

    @Test
    void testThrowsExceptionOnNotExistingUser() {
        AuthRequest request = new AuthRequest("NotForestWizard", "PassWord1234");
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.login(request)
        );
        assertEquals(Status.SUCH_USER_NOT_EXISTS, exception.getStatus());
    }

    @Test
    void testThrowsExceptionOnInvalidPassword() {
        AuthRequest registerRequest = new AuthRequest("ForestWizard", "PassWord1234");
        assertDoesNotThrow(() -> authService.register(registerRequest));
        AuthRequest loginRequest = new AuthRequest("ForestWizard", "PassWord123");
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authService.login(loginRequest)
        );
        assertEquals(Status.INVALID_PASSWORD, exception.getStatus());
    }

    @Test
    void testThrowsExceptionOnAuthRequest() {
        InvalidRequestException exception;

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(null)
        );
        assertEquals(Status.REQUEST_BODY_BAD_OR_MISSING, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest(null, "PassWord1234"))
        );
        assertEquals(Status.USERNAME_CANNOT_BE_NULL, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest("", "PassWord1234"))
        );
        assertEquals(Status.INVALID_USERNAME_LENGTH, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest("ForestWizard", null))
        );
        assertEquals(Status.PASSWORD_CANNOT_BE_NULL, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest("ForestWizard", ""))
        );
        assertEquals(Status.INVALID_PASSWORD_LENGTH, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest("ForestWizard", "simpleword"))
        );
        assertEquals(Status.PASSWORD_REQUIREMENTS_UNSATISFIED, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class,
                () -> authService.register(new AuthRequest("ForestWizard", "1234"))
        );
        assertEquals(Status.INVALID_PASSWORD_LENGTH, exception.getStatus());
    }
}
