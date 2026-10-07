package org.forestwizard.urlshortener.url;

import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.forestwizard.urlshortener.exception.*;
import org.forestwizard.urlshortener.status.Status;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UrlService {
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String SHORT_CODE_CONSTRAINT_NAME = "shortened_url_short_code_unique";
    private static final String SHORT_URL_MAPPING = "/link/%s";
    private static final int MAX_URL_GENERATION_ATTEMPTS = 10;

    private final IUserRepository userRepository;
    private final IShortenedUrlRepository shortenedUrlRepository;
    private final UrlTransactionService urlTransactionService;
    private final String baseUrl;

    public UrlService(
            @Value("${springwebapp.app.base_url}") String baseUrl,
            IUserRepository userRepository,
            IShortenedUrlRepository shortenedUrlRepository,
            UrlTransactionService urlTransactionService
    ) {
        this.userRepository = userRepository;
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.urlTransactionService = urlTransactionService;
        this.baseUrl = baseUrl;
    }

    public UrlListResponse getAllByUsername(String username, Boolean showExpired) throws UsernameNotFoundException {
        List<ShortenedUrl> urls;

        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.UNAUTHORIZED_REQUEST);
        }

        if (showExpired == null || !showExpired) {
            urls = shortenedUrlRepository.findAllValidByUsername(username);
        } else {
            urls = shortenedUrlRepository.findAllByUsername(username);
        }

        return UrlListResponse.of(Status.OK, username, baseUrl + SHORT_URL_MAPPING, urls);
    }

    public UrlResponse getByUsernameAndId(
            String username,
            Long id
    ) throws UsernameNotFoundException, UrlNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.UNAUTHORIZED_REQUEST);
        }

        Optional<ShortenedUrl> urlOptional = shortenedUrlRepository.findByUsernameAndId(username, id);
        return urlOptional.map(
                url -> UrlResponse.of(Status.OK, username, baseUrl + SHORT_URL_MAPPING, url)
        ).orElseThrow(() -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS));
    }

    public UrlResponse create(
            String username,
            UrlCreateRequest request
    ) throws UsernameNotFoundException, InvalidRequestException {
        int attempts = MAX_URL_GENERATION_ATTEMPTS;
        validateUrlRequest(request);
        AuthUser user = userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException(Status.UNAUTHORIZED_REQUEST)
        );

        while (attempts > 0) {
            try {
                ShortenedUrl url = urlTransactionService.saveWithNewRandomId(user, request);
                return UrlResponse.of(Status.OK, username, baseUrl + SHORT_URL_MAPPING, url);
            } catch (DataIntegrityViolationException e) {
                if (isCauseUniqueConstraintViolation(e)) {
                    attempts--;
                } else {
                    throw new UrlGenerationException(Status.URL_GENERATION_FAILED);
                }
            }
        }

        throw new UrlGenerationException(Status.URL_GENERATION_FAILED);
    }

    public UrlResponse update(
            String username,
            Long id,
            UrlCreateRequest request
    ) throws UsernameNotFoundException, UrlNotFoundException, InvalidRequestException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.UNAUTHORIZED_REQUEST);
        }
        validateUrlRequest(request);

        int updates = urlTransactionService.update(username, id, request);
        if (updates == 0) {
            throw new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS);
        }

        Optional<ShortenedUrl> urlOptional = shortenedUrlRepository.findByUsernameAndId(username, id);
        return urlOptional.map(
                url -> UrlResponse.of(Status.OK, username, baseUrl + SHORT_URL_MAPPING, url)
        ).orElseThrow(() -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS));
    }

    public StatusResponse deleteByUsernameAndId(
            String username,
            Long id
    ) throws UsernameNotFoundException, UrlNotFoundException {
        if (!userRepository.existsByUsername(username)) {
            throw new UsernameNotFoundException(Status.UNAUTHORIZED_REQUEST);
        }

        return shortenedUrlRepository.findByUsernameAndId(username, id).map(url -> {
            shortenedUrlRepository.delete(url);
            return new StatusResponse(Status.OK);
        }).orElseThrow(() -> new UrlNotFoundException(Status.SUCH_URL_NOT_EXISTS));
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

        if (request.getExpirationTimeMinutes() <= 0) {
            throw new InvalidRequestException(Status.INVALID_URL_EXPIRATION_TIME);
        }
    }

    private boolean isCauseUniqueConstraintViolation(Throwable exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException constraintException) {
                String constraint = constraintException.getConstraintName();
                return constraint != null && constraint.equalsIgnoreCase(SHORT_CODE_CONSTRAINT_NAME);
            }
            cause = cause.getCause();
        }
        return false;
    }
}
