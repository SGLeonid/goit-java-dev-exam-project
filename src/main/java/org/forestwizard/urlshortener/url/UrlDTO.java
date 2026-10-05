package org.forestwizard.urlshortener.url;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@Builder
public class UrlDTO {
    private final Long id;
    private final String shortUrl;
    private final String originalUrl;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime expiresAt;
    private final Long visitTimes;
}
