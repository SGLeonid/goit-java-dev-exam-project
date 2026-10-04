package org.forestwizard.urlshortener.url;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.forestwizard.urlshortener.status.Status;

import java.util.List;

@Data
@AllArgsConstructor
public class UrlListResponse {
    @Enumerated(EnumType.STRING)
    private final Status error;
    private final String createdBy;
    private final List<ShortenedUrl> urls;
}
