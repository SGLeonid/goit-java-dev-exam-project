package org.forestwizard.urlshortener.url;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlCreateRequest {
    private String originalUrl;
    private Integer expirationTimeMinutes;
}
