package org.forestwizard.urlshortener;


import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.AuthUserDetailsService;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.auth.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {
    @Mock
    private IUserRepository userRepository;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testFindsUserByUsername() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        given(userRepository.findByUsername("ForestWizard")).willReturn(Optional.of(user));

        UserDetails loadedUser = assertDoesNotThrow(() -> authUserDetailsService.loadUserByUsername("ForestWizard"));
        assertNotNull(loadedUser);

        assertEquals("ForestWizard", loadedUser.getUsername());
        assertEquals("PassWord1234", loadedUser.getPassword());
        assertEquals(Set.of(new SimpleGrantedAuthority(Role.USER.name())), loadedUser.getAuthorities());
        verify(userRepository).findByUsername("ForestWizard");
    }

    @Test
    void testHasUser() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);

        boolean exists = assertDoesNotThrow(() -> authUserDetailsService.hasUser("ForestWizard"));
        assertTrue(exists);

        exists = assertDoesNotThrow(() -> authUserDetailsService.hasUser("NotForestWizard"));
        assertFalse(exists);

        verify(userRepository).existsByUsername("ForestWizard");
    }

    @Test
    void testSavesUser() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);

        given(userRepository.insert("ForestWizard", "PassWord1234", Role.USER.name())).willReturn(1);
        int insertedRows = authUserDetailsService.saveUser(user);
        assertEquals(1, insertedRows);
        verify(userRepository).insert("ForestWizard", "PassWord1234", Role.USER.name());
    }

    @Test
    void testThrowsExceptionWhenUserNotExists() {
        given(userRepository.findByUsername("NotForestWizard")).willReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername("NotForestWizard")
        );
        verify(userRepository).findByUsername("NotForestWizard");
    }
}
