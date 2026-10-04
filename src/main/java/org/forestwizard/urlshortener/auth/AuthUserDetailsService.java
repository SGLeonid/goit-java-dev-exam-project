package org.forestwizard.urlshortener.auth;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AuthUser> optional = userRepository.findByUsername(username);
        AuthUser user = optional.orElseThrow(() -> new UsernameNotFoundException("Such user not found: " + username));
        return new User(user.getUsername(), user.getPasswordHash(), Collections.emptyList());
    }

    public boolean hasUser(String username) {
        return userRepository.existsByUsername(username);
    }

    public AuthUser saveUser(AuthUser user) {
        return userRepository.save(user);
    }
}
