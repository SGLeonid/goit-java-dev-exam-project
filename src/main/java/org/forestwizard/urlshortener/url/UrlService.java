package org.forestwizard.urlshortener.url;

import jakarta.transaction.Transactional;
import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.forestwizard.urlshortener.exception.*;
import org.forestwizard.urlshortener.status.Status;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlService {
    private static final String SHORT_URL_TEMPLATE = "http://localhost:8080/link/%s";
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";

    private final IUserRepository userRepository;
    private final IShortenedUrlRepository shortenedUrlRepository;
    private final Random random;

    public UrlService(IUserRepository userRepository, IShortenedUrlRepository shortenedUrlRepository) {
        this.userRepository = userRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.random = new Random();
    }

    public UrlListResponse getAllByUsername(String username) throws UsernameNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.SUCH_USER_NOT_EXISTS);
        }

        List<ShortenedUrl> urls = shortenedUrlRepository.findAllByUsername(username);
        return new UrlListResponse(Status.OK, username, urls);
    }

    public UrlResponse getByUsernameAndId(
            String username,
            Long id
    ) throws UsernameNotFoundException, UrlNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.SUCH_USER_NOT_EXISTS);
        }

        Optional<ShortenedUrl> urlOptional = shortenedUrlRepository.findByUsernameAndId(username, id);
        return urlOptional.map(url -> new UrlResponse(Status.OK, username, url)).orElseThrow(
                () -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS)
        );
    }

    public UrlResponse create(
            String username,
            UrlCreateRequest request
    ) throws UsernameNotFoundException, InvalidRequestException {
        validateUrlRequest(request);

        OffsetDateTime createdAt = OffsetDateTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MICROS);
        OffsetDateTime expiresAt = createdAt.plusMinutes(request.getExpirationTimeMinutes());
        AuthUser user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(Status.SUCH_USER_NOT_EXISTS)
        );

        String shortUrl;
        do {
            shortUrl = String.format(SHORT_URL_TEMPLATE, generateRandomId());
        } while(shortenedUrlRepository.existsByShortUrl(shortUrl));

        ShortenedUrl url = shortenedUrlRepository.save(ShortenedUrl.builder()
                .user(user)
                .originalUrl(request.getOriginalUrl())
                .shortUrl(shortUrl)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .visitTimes(0L)
                .build()
        );
        return new UrlResponse(Status.OK, username, url);
    }

    @Transactional(rollbackOn = Exception.class)
    public UrlResponse update(
            String username,
            Long id,
            UrlCreateRequest request
    ) throws UsernameNotFoundException, UrlNotFoundException, InvalidRequestException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.SUCH_USER_NOT_EXISTS);
        }
        validateUrlRequest(request);

        Instant createdAtInstant = shortenedUrlRepository.findCreatedAtByUsernameAndId(username, id).orElseThrow(
                () -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS)
        );

        OffsetDateTime createdAt = OffsetDateTime.ofInstant(createdAtInstant, ZoneId.of("UTC"));
        OffsetDateTime expiresAt = createdAt.plusMinutes(request.getExpirationTimeMinutes());
        shortenedUrlRepository.updateByUsernameAndId(username, id, request.getOriginalUrl(), expiresAt);
        Optional<ShortenedUrl> urlOptional = shortenedUrlRepository.findByUsernameAndId(username, id);
        return urlOptional.map(shortenedUrl -> new UrlResponse(Status.OK, username, shortenedUrl)).orElseThrow(
                () -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS)
        );
    }

    public StatusResponse deleteByUsernameAndId(
            String username,
            Long id
    ) throws UsernameNotFoundException, UrlNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.SUCH_USER_NOT_EXISTS);
        }

        return shortenedUrlRepository.findByUsernameAndId(username, id).map(url -> {
            shortenedUrlRepository.delete(url);
            return new StatusResponse(Status.OK);
        }).orElseThrow(() -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS));
    }

    @Transactional(rollbackOn = Exception.class)
    public String getOriginalUrl(String uniqueId) throws RedirectUrlNotFoundException, RedirectUrlExpiredException {
        String url = String.format(SHORT_URL_TEMPLATE, uniqueId);
        ShortenedUrl shortenedUrl = shortenedUrlRepository.findByShortUrl(url).orElseThrow(
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

    public void validateUrlRequest(UrlCreateRequest request) throws InvalidRequestException {
        if (request == null) {
            throw new InvalidRequestException(Status.REQUEST_BODY_BAD_OR_MISSING);
        }

        String url = request.getOriginalUrl();
        if (url == null) {
            throw new InvalidRequestException(Status.ORIGINAL_URL_CANNOT_BE_NULL);
        }

        if (url.trim().isEmpty()) {
            throw new InvalidRequestException(Status.ORIGINAL_URL_CANNOT_BE_EMPTY);
        }

        if (!(url.startsWith(HTTP_PREFIX) || url.startsWith(HTTPS_PREFIX))) {
            throw new InvalidRequestException(Status.INVALID_ORIGINAL_URL_FORMAT);
        }

        if (request.getExpirationTimeMinutes() == null) {
            throw new InvalidRequestException(Status.URL_EXPIRATION_TIME_CANNOT_BE_NULL);
        }

        if (request.getExpirationTimeMinutes() < 0) {
            throw new InvalidRequestException(Status.INVALID_URL_EXPIRATION_TIME);
        }
    }

    private String generateRandomId() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            builder.append(Integer.toString(random.nextInt(0, Character.MAX_RADIX), Character.MAX_RADIX));
        }
        return builder.toString();
    }
}
