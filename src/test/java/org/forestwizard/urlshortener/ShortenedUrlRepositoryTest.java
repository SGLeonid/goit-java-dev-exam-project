package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.AuthUser;
import org.forestwizard.urlshortener.auth.IUserRepository;
import org.forestwizard.urlshortener.auth.Role;
import org.forestwizard.urlshortener.url.IShortenedUrlRepository;
import org.forestwizard.urlshortener.url.ShortenedUrl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ShortenedUrlRepositoryTest {
    private static final String TEST_EXAMPLE_ORIGINAL_URL = "https://www.baeldung.com/rest-versioning";
    private static final String TEST_EXAMPLE_SHORT_URL = "http://localhost:8080/h4xs8g3";
    private static final String TEST_EXAMPLE_NEW_ORIGINAL_URL = "https://www.baeldung.com/mockito-series";
    private static final OffsetDateTime TEST_EXAMPLE_CREATE_DATE = OffsetDateTime
            .now(TimeZone.getTimeZone("UTC").toZoneId())
            .truncatedTo(ChronoUnit.MICROS);

    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IShortenedUrlRepository shortenedUrlRepository;
    @Autowired
    TestEntityManager entityManager;

    @BeforeAll
    static void setUpBeforeAll() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    void setUp() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        userRepository.save(user);

        OffsetDateTime createdAt = TEST_EXAMPLE_CREATE_DATE;
        OffsetDateTime expiresAt = createdAt.plusMinutes(60);

        ShortenedUrl url = createShortUrl(user, createdAt, expiresAt);
        shortenedUrlRepository.save(url);
    }

    @AfterEach
    void cleanUp() {
        shortenedUrlRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testFindsAllByUsername() {
        List<ShortenedUrl> urls = assertDoesNotThrow(() -> shortenedUrlRepository.findAllByUsername("ForestWizard"));
        assertNotNull(urls);
        assertEquals(1, urls.size());
    }

    @Test
    void testFindsByUsernameAndId() {
        Optional<AuthUser> userOptional = assertDoesNotThrow(() -> userRepository.findByUsername("ForestWizard"));
        assertTrue(userOptional.isPresent());
        OffsetDateTime createdAt = OffsetDateTime
                .now(TimeZone.getTimeZone("UTC").toZoneId())
                .truncatedTo(ChronoUnit.MICROS);
        ShortenedUrl url = assertDoesNotThrow(() -> shortenedUrlRepository.save(createShortUrl(
                userOptional.get(),
                createdAt,
                createdAt.plusMinutes(60)
        )));
        Optional<ShortenedUrl> selectedUrl = assertDoesNotThrow(
                () -> shortenedUrlRepository.findByUsernameAndId("ForestWizard", url.getId())
        );
        assertTrue(selectedUrl.isPresent());
        assertEquals(url, selectedUrl.get());
    }

    @Test
    void testFindsCreatedAtByUsernameAndId() {
        Optional<AuthUser> userOptional = assertDoesNotThrow(() -> userRepository.findByUsername("ForestWizard"));
        assertTrue(userOptional.isPresent());
        OffsetDateTime createdAt = OffsetDateTime
                .now(TimeZone.getTimeZone("UTC").toZoneId())
                .truncatedTo(ChronoUnit.MICROS);
        ShortenedUrl url = assertDoesNotThrow(() -> shortenedUrlRepository.save(createShortUrl(
                userOptional.get(),
                createdAt,
                createdAt.plusMinutes(60)
        )));
        Optional<Instant> instantOptional = assertDoesNotThrow(() ->shortenedUrlRepository.findCreatedAtByUsernameAndId(
                "ForestWizard", url.getId()
        ));
        assertTrue(instantOptional.isPresent());
        OffsetDateTime savedCreatedAt = OffsetDateTime.ofInstant(
                instantOptional.get(), TimeZone.getDefault().toZoneId()
        );
        assertEquals(url.getCreatedAt(), savedCreatedAt);
    }

    @Test
    void testUpdatesByUsernameAndId() {
        Optional<AuthUser> userOptional = assertDoesNotThrow(() -> userRepository.findByUsername("ForestWizard"));
        assertTrue(userOptional.isPresent());
        OffsetDateTime createdAt = OffsetDateTime
                .now(TimeZone.getTimeZone("UTC").toZoneId())
                .truncatedTo(ChronoUnit.MICROS);
        ShortenedUrl url = createShortUrl(userOptional.get(), createdAt, createdAt.plusMinutes(60));
        url = entityManager.persistAndFlush(url);
        OffsetDateTime newExpirationTime = url.getCreatedAt().plusMinutes(240);
        long id = url.getId();
        int updatedRows = shortenedUrlRepository.updateByUsernameAndId(
                "ForestWizard",
                id,
                TEST_EXAMPLE_NEW_ORIGINAL_URL,
                newExpirationTime
        );
        entityManager.clear();

        assertEquals(1, updatedRows);

        Optional<ShortenedUrl> newUrlOptional = assertDoesNotThrow(() -> shortenedUrlRepository.findByUsernameAndId(
                "ForestWizard", id
        ));
        assertTrue(newUrlOptional.isPresent());
        ShortenedUrl newSavedUrl = newUrlOptional.get();

        assertEquals(TEST_EXAMPLE_NEW_ORIGINAL_URL, newSavedUrl.getOriginalUrl());
        assertEquals(newExpirationTime, newSavedUrl.getExpiresAt());
    }

    @Test
    void testIncrementsVisitTimesById() {
        Optional<AuthUser> userOptional = userRepository.findByUsername("ForestWizard");
        assertTrue(userOptional.isPresent());
        OffsetDateTime createdAt = OffsetDateTime
                .now(TimeZone.getTimeZone("UTC").toZoneId())
                .truncatedTo(ChronoUnit.MICROS);
        ShortenedUrl url = createShortUrl(userOptional.get(), createdAt, createdAt.plusMinutes(60));
        url = entityManager.persistAndFlush(url);
        long countBeforeIncrement = url.getVisitTimes();
        int updatedRows = shortenedUrlRepository.incrementUrlVisitTimesById(url.getId());
        entityManager.clear();

        assertEquals(1, updatedRows);

        Optional<ShortenedUrl> updatedUrlOptional = shortenedUrlRepository.findByUsernameAndId("ForestWizard", url.getId());
        assertTrue(updatedUrlOptional.isPresent());
        ShortenedUrl updatedUrl = updatedUrlOptional.get();
        long countAfterIncrement = updatedUrl.getVisitTimes();

        assertEquals(countBeforeIncrement + 1, countAfterIncrement);
    }

    @Test
    void testFindsByShortUrl() {
        Optional<ShortenedUrl> savedUrlOptional = assertDoesNotThrow(
                () -> shortenedUrlRepository.findByShortUrl(TEST_EXAMPLE_SHORT_URL)
        );
        assertTrue(savedUrlOptional.isPresent());
        ShortenedUrl savedUrl = savedUrlOptional.get();

        assertEquals(new AuthUser("ForestWizard", "PassWord1234", Role.USER), savedUrl.getUser());
        assertEquals(TEST_EXAMPLE_SHORT_URL, savedUrl.getShortUrl());
        assertEquals(TEST_EXAMPLE_ORIGINAL_URL, savedUrl.getOriginalUrl());
        assertEquals(TEST_EXAMPLE_CREATE_DATE, savedUrl.getCreatedAt());
        assertEquals(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60), savedUrl.getExpiresAt());
        assertEquals(0L, savedUrl.getVisitTimes());
    }

    @Test
    void testExistsByShortUrl() {
        boolean exists = assertDoesNotThrow(() -> shortenedUrlRepository.existsByShortUrl(TEST_EXAMPLE_SHORT_URL));
        assertTrue(exists);
    }

    private ShortenedUrl createShortUrl(AuthUser user, OffsetDateTime createdAt, OffsetDateTime expiresAt) {
        return ShortenedUrl.builder()
                .user(user)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortUrl(TEST_EXAMPLE_SHORT_URL)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .visitTimes(0L)
                .build();
    }
}
