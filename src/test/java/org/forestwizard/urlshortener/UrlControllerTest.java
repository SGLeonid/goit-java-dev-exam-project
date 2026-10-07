package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.*;
import org.forestwizard.urlshortener.exception.GlobalExceptionHandler;
import org.forestwizard.urlshortener.url.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UrlControllerTest {
    private static final String TEST_EXAMPLE_ORIGINAL_URL = "https://www.baeldung.com/rest-versioning";
    private static final String TEST_EXAMPLE_MISSING_URL = "http://localhost:8080/link/missing";
    private static final String TEST_EXAMPLE_SHORT_URL = "http://localhost:8080/link/h4xs8g3";
    private static final String TEST_EXAMPLE_EXPIRED_URL = "http://localhost:8080/link/explink";
    private static final String TEST_EXAMPLE_SHORT_CODE = "h4xs8g3";
    private static final String TEST_EXAMPLE_EXPIRED_CODE = "explink";
    private static final String TEST_CONTROLLER_NOT_FOUND = "/notfound";
    private static final String TEST_CONTROLLER_EXPIRED = "/expired";
    private static final OffsetDateTime TEST_EXAMPLE_CREATE_DATE = OffsetDateTime
            .now(TimeZone.getTimeZone("UTC").toZoneId())
            .truncatedTo(ChronoUnit.MICROS);
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IShortenedUrlRepository shortenedUrlRepository;
    @Autowired
    private UrlTransactionService urlTransactionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UrlController(urlTransactionService))
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
        AuthUser user = userRepository.save(new AuthUser("ForestWizard", "PassWord1234", Role.USER));
        shortenedUrlRepository.save(ShortenedUrl.builder()
                .user(user)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(60))
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortCode(TEST_EXAMPLE_SHORT_CODE)
                .visitTimes(0L)
                .build());
        shortenedUrlRepository.save(ShortenedUrl.builder()
                .user(user)
                .createdAt(TEST_EXAMPLE_CREATE_DATE.minusMinutes(60))
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.minusMinutes(10))
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortCode(TEST_EXAMPLE_EXPIRED_CODE)
                .visitTimes(0L)
                .build());
    }

    @AfterEach
    void cleanUp() {
        shortenedUrlRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testReturnsOriginalUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TEST_EXAMPLE_SHORT_URL))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", TEST_EXAMPLE_ORIGINAL_URL));
    }

    @Test
    void testThrowsOnNonExistingUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TEST_EXAMPLE_MISSING_URL))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", TEST_CONTROLLER_NOT_FOUND));
    }

    @Test
    void testThrowsOnExpiredUrl() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(TEST_EXAMPLE_EXPIRED_URL))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", TEST_CONTROLLER_EXPIRED));
    }
}
