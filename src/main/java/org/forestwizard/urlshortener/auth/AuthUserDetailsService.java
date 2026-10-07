package org.forestwizard.urlshortener.auth;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {
    private final IUserRepository userRepository;

    public AuthUser findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Such user not found: " + username)
        );
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AuthUser> optional = userRepository.findByUsername(username);
        AuthUser user = optional.orElseThrow(() -> new UsernameNotFoundException("Such user not found: " + username));
        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    public boolean hasUser(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(rollbackOn = Exception.class)
    public int saveUser(AuthUser user) throws DataIntegrityViolationException {
        return userRepository.insert(user.getUsername(), user.getPasswordHash(), user.getRole().name());
    }
}
