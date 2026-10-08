package org.forestwizard.urlshortener.url;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.forestwizard.urlshortener.status.Status;

@Data
@AllArgsConstructor
public class UrlResponse {
    private final Status error;
    private final String createdBy;
    private final UrlDTO url;

    public static UrlResponse of(Status status, String createdBy, String urlFormat, ShortenedUrl url) {
        UrlDTO dto = UrlDTO.builder()
                .id(url.getId())
                .shortUrl(String.format(urlFormat, url.getShortCode()))
                .originalUrl(url.getOriginalUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .visitTimes(url.getVisitTimes())
                .build();
        return new UrlResponse(status, createdBy, dto);
    }
}
