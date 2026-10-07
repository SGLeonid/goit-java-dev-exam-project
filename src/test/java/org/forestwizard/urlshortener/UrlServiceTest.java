package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.auth.Role;
import org.forestwizard.urlshortener.exception.*;
import org.forestwizard.urlshortener.status.Status;
import org.forestwizard.urlshortener.url.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {
    private static final String TEST_EXAMPLE_ORIGINAL_URL = "https://www.baeldung.com/rest-versioning";
    private static final String TEST_EXAMPLE_SHORT_CODE = "h4xs8g3";
    private static final String TEST_EXAMPLE_SHORT_URL = "http://localhost:8080/link/h4xs8g3";
    private static final String TEST_EXAMPLE_SHORT_URL_PATH_ID = "h4xs8g3";
    private static final String TEST_EXAMPLE_NEW_ORIGINAL_URL = "https://www.baeldung.com/mockito-series";
    private static final OffsetDateTime TEST_EXAMPLE_CREATE_DATE = OffsetDateTime
            .now(TimeZone.getTimeZone("UTC").toZoneId())
            .truncatedTo(ChronoUnit.MICROS);

    @Mock
    private IUserRepository userRepository;
    @Mock
    private IShortenedUrlRepository shortenedUrlRepository;
    @InjectMocks
    private UrlTransactionService urlTransactionService;
    @InjectMocks
    private UrlService urlService;

    @BeforeEach
    void setUp() {
        urlService = new UrlService(
                "http://localhost:8080",
                userRepository,
                shortenedUrlRepository,
                urlTransactionService
        );
    }

    @Test
    void testGetAllByUsername() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .id(1L)
                .user(user)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortCode(TEST_EXAMPLE_SHORT_CODE)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .build();
        UrlDTO urlDto = UrlDTO.builder()
                .id(1L)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortUrl(TEST_EXAMPLE_SHORT_URL)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .build();
        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.findAllByUsername("ForestWizard")).willReturn(List.of(shortenedUrl));

        UrlListResponse response = assertDoesNotThrow(() -> urlService.getAllByUsername("ForestWizard", true));
        assertNotNull(response);
        assertEquals(Status.OK, response.getError());
        assertEquals(List.of(urlDto), response.getUrls());

        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).findAllByUsername("ForestWizard");
    }

    @Test
    void testGetAllByUsernameThrowsExceptions() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(false);
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> urlService.getAllByUsername("ForestWizard", false)
        );
        assertEquals(Status.UNAUTHORIZED_REQUEST, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
    }


    @Test
    void testGetByUsernameAndId() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder().id(1L).user(user).build();
        UrlResponse expectedResponse = UrlResponse.of(
                Status.OK,
                "ForestWizard",
                "http://localhost:8080/link/%s",
                shortenedUrl
        );

        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.findByUsernameAndId("ForestWizard", 1L)).willReturn(Optional.of(shortenedUrl));

        UrlResponse response = assertDoesNotThrow(() -> urlService.getByUsernameAndId("ForestWizard", 1L));
        assertNotNull(response);
        assertEquals(expectedResponse, response);

        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).findByUsernameAndId("ForestWizard", 1L);
    }

    @Test
    void testGetByUsernameAndIdThrowsUsernameNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(false);
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> urlService.getByUsernameAndId("ForestWizard", 1L)
        );
        assertEquals(Status.UNAUTHORIZED_REQUEST, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
    }

    @Test
    void testGetByUsernameAndIdThrowsUrlNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.findByUsernameAndId("ForestWizard", 1L)).willReturn(Optional.empty());
        UrlNotFoundException exception = assertThrows(UrlNotFoundException.class,
                () -> urlService.getByUsernameAndId("ForestWizard", 1L)
        );
        assertEquals(Status.SUCH_URL_NOT_EXISTS, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).findByUsernameAndId("ForestWizard", 1L);
    }

    @Test
    void testCreate() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder().id(1L).user(user).build();
        UrlResponse expectedResponse = UrlResponse.of(
                Status.OK,
                "ForestWizard",
                "http://localhost:8080/link/%s",
                shortenedUrl
        );

        given(userRepository.findByUsername("ForestWizard")).willReturn(Optional.of(user));
        given(shortenedUrlRepository.saveAndFlush(any(ShortenedUrl.class))).willReturn(shortenedUrl);

        UrlResponse response = assertDoesNotThrow(
                () -> urlService.create("ForestWizard", new UrlCreateRequest(TEST_EXAMPLE_ORIGINAL_URL, 60))
        );
        assertEquals(expectedResponse, response);

        verify(userRepository).findByUsername("ForestWizard");
        verify(shortenedUrlRepository).saveAndFlush(any(ShortenedUrl.class));
    }

    @Test
    void testCreateThrowsUsernameNotFoundException() {
        given(userRepository.findByUsername("ForestWizard")).willReturn(Optional.empty());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> urlService.create("ForestWizard", new UrlCreateRequest(TEST_EXAMPLE_ORIGINAL_URL, 60))
        );
        assertEquals(Status.UNAUTHORIZED_REQUEST, exception.getStatus());
        verify(userRepository).findByUsername("ForestWizard");
    }

    @Test
    void testUpdate() {
        ShortenedUrl expectedUrl = ShortenedUrl.builder()
                .id(1L)
                .user(new AuthUser("ForestWizard", "PassWord1234", Role.USER))
                .originalUrl(TEST_EXAMPLE_NEW_ORIGINAL_URL)
                .shortCode(TEST_EXAMPLE_SHORT_CODE)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .build();
        UrlResponse expectedResponse = UrlResponse.of(
                Status.OK,
                "ForestWizard",
                "http://localhost:8080/link/" + TEST_EXAMPLE_SHORT_CODE,
                expectedUrl
        );

        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.updateByUsernameAndId(
                any(String.class),
                any(Long.class),
                any(String.class),
                any(OffsetDateTime.class))
        ).willReturn(1);
        given(shortenedUrlRepository.findByUsernameAndId("ForestWizard", 1L)).willReturn(Optional.of(expectedUrl));

        UrlResponse response = assertDoesNotThrow(() -> urlService.update("ForestWizard", 1L, new UrlCreateRequest(
                TEST_EXAMPLE_NEW_ORIGINAL_URL, 60
        )));
        assertEquals(expectedResponse, response);

        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).updateByUsernameAndId(
                any(String.class),
                any(Long.class),
                any(String.class),
                any(OffsetDateTime.class)
        );
        verify(shortenedUrlRepository).findByUsernameAndId("ForestWizard", 1L);
    }

    @Test
    void testUpdateThrowsUsernameNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(false);
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> urlService.update("ForestWizard", 1L, new UrlCreateRequest(TEST_EXAMPLE_NEW_ORIGINAL_URL, 60))
        );
        assertEquals(Status.UNAUTHORIZED_REQUEST, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
    }

    @Test
    void testUpdateThrowsUrlNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        UrlNotFoundException exception = assertThrows(UrlNotFoundException.class,
                () -> urlService.update("ForestWizard", 1L, new UrlCreateRequest(TEST_EXAMPLE_NEW_ORIGINAL_URL, 60))
        );
        assertEquals(Status.SUCH_URL_NOT_EXISTS, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
    }


    @Test
    void testDelete() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder().id(1L)
                .user(user)
                .shortCode(TEST_EXAMPLE_SHORT_CODE)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .build();
        shortenedUrlRepository.save(shortenedUrl);

        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.findByUsernameAndId("ForestWizard", 1L)).willReturn(Optional.of(shortenedUrl));
        assertDoesNotThrow(() -> urlService.deleteByUsernameAndId("ForestWizard", 1L));
        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).findByUsernameAndId("ForestWizard", 1L);
    }

    @Test
    void testDeleteThrowsUsernameNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(false);
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> urlService.deleteByUsernameAndId("ForestWizard", 1L)
        );
        assertEquals(Status.UNAUTHORIZED_REQUEST, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
    }

    @Test
    void testDeleteThrowsUrlNotFoundException() {
        given(userRepository.existsByUsername("ForestWizard")).willReturn(true);
        given(shortenedUrlRepository.findByUsernameAndId("ForestWizard", 123L)).willReturn(Optional.empty());
        UrlNotFoundException exception = assertThrows(
                UrlNotFoundException.class, () -> urlService.deleteByUsernameAndId("ForestWizard", 123L)
        );
        assertEquals(Status.SUCH_URL_NOT_EXISTS, exception.getStatus());
        verify(userRepository).existsByUsername("ForestWizard");
        verify(shortenedUrlRepository).findByUsernameAndId("ForestWizard", 123L);
    }

    @Test
    void testGetOriginalUrl() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        ShortenedUrl shortenedUrl = ShortenedUrl.builder()
                .id(1L)
                .user(user)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortCode(TEST_EXAMPLE_SHORT_CODE)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .build();
        given(shortenedUrlRepository.findByShortCode(TEST_EXAMPLE_SHORT_CODE)).willReturn(Optional.of(shortenedUrl));
        String url = assertDoesNotThrow(() -> urlTransactionService.getOriginalUrl(TEST_EXAMPLE_SHORT_URL_PATH_ID));
        assertEquals(TEST_EXAMPLE_ORIGINAL_URL, url);
        verify(shortenedUrlRepository).findByShortCode(TEST_EXAMPLE_SHORT_CODE);
    }

    @Test
    void testGetOriginalUrlThrowsUrlNotFoundExceptions() {
        given(shortenedUrlRepository.findByShortCode(any(String.class))).willReturn(Optional.empty());
        RedirectUrlNotFoundException notFoundException = assertThrows(
                RedirectUrlNotFoundException.class,
                () -> urlTransactionService.getOriginalUrl(TEST_EXAMPLE_SHORT_URL_PATH_ID)
        );
        assertEquals("URL not found", notFoundException.getMessage());
        verify(shortenedUrlRepository).findByShortCode(any(String.class));
    }

    @Test
    void testGetOriginalUrlThrowsUrlExpiredException() {
        given(shortenedUrlRepository.findByShortCode(any(String.class))).willReturn(Optional.of(ShortenedUrl.builder()
                .createdAt(OffsetDateTime.now(TimeZone.getDefault().toZoneId()).minusMinutes(60))
                .expiresAt(OffsetDateTime.now(TimeZone.getDefault().toZoneId()).minusMinutes(10))
                .build()
        ));
        RedirectUrlExpiredException urlExpiredException = assertThrows(
                RedirectUrlExpiredException.class,
                () -> urlTransactionService.getOriginalUrl(TEST_EXAMPLE_SHORT_URL_PATH_ID)
        );
        assertEquals("URL is expired", urlExpiredException.getMessage());
        verify(shortenedUrlRepository).findByShortCode(any(String.class));
    }

    @Test
    void testThrowsOnInvalidUrlCreateRequest() {
        InvalidRequestException exception;

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create("ForestWizard", null));
        assertEquals(Status.REQUEST_BODY_BAD_OR_MISSING, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create(
                "ForestWizard", new UrlCreateRequest(null, 60)
        ));
        assertEquals(Status.ORIGINAL_URL_CANNOT_BE_NULL, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create(
                "ForestWizard", new UrlCreateRequest("", 60)
        ));
        assertEquals(Status.ORIGINAL_URL_CANNOT_BE_EMPTY, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create(
                "ForestWizard", new UrlCreateRequest("unknownprotocol://www.javadev.com", 60)
        ));
        assertEquals(Status.INVALID_ORIGINAL_URL_FORMAT, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create(
                "ForestWizard", new UrlCreateRequest("https://www.javadev.com", null)
        ));
        assertEquals(Status.URL_EXPIRATION_TIME_CANNOT_BE_NULL, exception.getStatus());

        exception = assertThrows(InvalidRequestException.class, () -> urlService.create(
                "ForestWizard", new UrlCreateRequest("https://www.javadev.com", -1)
        ));
        assertEquals(Status.INVALID_URL_EXPIRATION_TIME, exception.getStatus());
    }

}
