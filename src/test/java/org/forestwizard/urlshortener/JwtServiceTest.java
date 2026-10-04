package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.AuthUserDetailsService;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.auth.Role;
import org.forestwizard.urlshortener.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class JwtServiceTest {
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private AuthUserDetailsService authUserDetailsService;
    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        authUserDetailsService.saveUser(new AuthUser("ForestWizard", "PassWord1234", Role.USER));
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testGeneratesValidToken() {
        String token = jwtService.generateToken("ForestWizard");
        assertEquals("ForestWizard", jwtService.extractUsername(token));
        boolean isValid = assertDoesNotThrow(
                () -> jwtService.validateToken(token, authUserDetailsService.loadUserByUsername("ForestWizard"))
        );
        assertTrue(isValid);
    }
}
