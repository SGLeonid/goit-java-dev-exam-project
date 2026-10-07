package org.forestwizard.urlshortener.url;

import jakarta.transaction.Transactional;
import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.exception.RedirectUrlExpiredException;
import org.forestwizard.urlshortener.exception.RedirectUrlNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
public class UrlTransactionService {
    private final IShortenedUrlRepository shortenedUrlRepository;
    private final SecureRandom secureRandom;

    public UrlTransactionService(IShortenedUrlRepository shortenedUrlRepository) {
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.secureRandom = new SecureRandom();
    }

    @Transactional(rollbackOn = Exception.class)
    public ShortenedUrl saveWithNewRandomId(
            AuthUser user,
            UrlCreateRequest request
    ) throws DataIntegrityViolationException {
        String shortCode = generateRandomId();
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime expiresAt = createdAt.plusMinutes(request.getExpirationTimeMinutes());
        return shortenedUrlRepository.saveAndFlush(ShortenedUrl.builder()
                .user(user)
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .visitTimes(0L)
                .build()
        );
    }

    @Transactional(rollbackOn = Exception.class)
    public int update(String username, Long id, UrlCreateRequest request) {
        OffsetDateTime createdAt = OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime expiresAt = createdAt.plusMinutes(request.getExpirationTimeMinutes());
        return shortenedUrlRepository.updateByUsernameAndId(username, id, request.getOriginalUrl(), expiresAt);
    }

    @Transactional(rollbackOn = Exception.class)
    public String getOriginalUrl(String uniqueId) throws RedirectUrlNotFoundException, RedirectUrlExpiredException {
        ShortenedUrl shortenedUrl = shortenedUrlRepository.findByShortCode(uniqueId).orElseThrow(
                () -> new RedirectUrlNotFoundException("URL not found")
        );

        if (!OffsetDateTime.now(ZoneId.systemDefault())
                .truncatedTo(ChronoUnit.MICROS)
                .isBefore(shortenedUrl.getExpiresAt())
        ) {
            throw new RedirectUrlExpiredException("URL is expired");
        }

        shortenedUrlRepository.incrementUrlVisitTimesById(shortenedUrl.getId());
        return shortenedUrl.getOriginalUrl();
    }

    private String generateRandomId() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            builder.append(Integer.toString(secureRandom.nextInt(0, Character.MAX_RADIX), Character.MAX_RADIX));
        }
        return builder.toString();
    }
}
