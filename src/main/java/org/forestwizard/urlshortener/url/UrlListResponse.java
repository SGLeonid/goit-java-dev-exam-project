package org.forestwizard.urlshortener.url;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.forestwizard.urlshortener.status.Status;

import java.util.List;

@Data
@AllArgsConstructor
public class UrlListResponse {
    private final Status error;
    private final String createdBy;
    private final List<UrlDTO> urls;

    public static UrlListResponse of(Status error, String createdBy, String urlFormat, List<ShortenedUrl> urls) {
        List<UrlDTO> urlList = urls.stream().map(url -> UrlDTO.builder()
                .id(url.getId())
                .shortUrl(String.format(urlFormat, url.getShortCode()))
                .originalUrl(url.getOriginalUrl())
                .createdAt(url.getCreatedAt())
                .expiresAt(url.getExpiresAt())
                .visitTimes(url.getVisitTimes())
                .build()
        ).toList();
        return new UrlListResponse(error, createdBy, urlList);
    }
}
