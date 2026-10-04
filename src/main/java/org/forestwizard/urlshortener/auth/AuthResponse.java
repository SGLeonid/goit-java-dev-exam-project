package org.forestwizard.urlshortener.auth;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.forestwizard.urlshortener.status.Status;

@Data
@AllArgsConstructor
public class AuthResponse {
    @Enumerated(EnumType.STRING)
    private final Status error;
    private final String token;
}
