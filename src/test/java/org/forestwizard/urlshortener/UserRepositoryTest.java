package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.auth.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {
    @Autowired
    private IUserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSavesUser() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        AuthUser savedUser = assertDoesNotThrow(() -> userRepository.save(user));
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(1, userRepository.count());
    }

    @Test
    void testExistsSavedUser() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        userRepository.save(user);
        boolean exists = assertDoesNotThrow(() -> userRepository.existsByUsername("ForestWizard"));
        assertTrue(exists);
    }

    @Test
    void testFindsUserByUsername() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        userRepository.save(user);

        Optional<AuthUser> userOptional = assertDoesNotThrow(() -> userRepository.findByUsername("ForestWizard"));
        assertTrue(userOptional.isPresent());
        AuthUser savedUser = userOptional.get();
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getPassword(), savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());
    }
}
