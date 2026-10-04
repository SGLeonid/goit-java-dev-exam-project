package org.forestwizard.urlshortener.status;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class StatusResponse {
    @Enumerated(EnumType.STRING)
    private final Status error;
}
