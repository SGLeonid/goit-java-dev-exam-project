package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.*;
import org.forestwizard.urlshortener.status.Status;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.forestwizard.urlshortener.url.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccountControllerTest {
    private static final String TEST_EXAMPLE_ORIGINAL_URL = "https://www.baeldung.com/rest-versioning";
    private static final String TEST_EXAMPLE_NEW_ORIGINAL_URL = "https://www.baeldung.com/mockito-series";
    private static final String TEST_EXAMPLE_SHORT_URL = "http://localhost:8080/h4xs8g3";
    private static final OffsetDateTime TEST_EXAMPLE_CREATE_DATE = OffsetDateTime
            .now(TimeZone.getTimeZone("UTC").toZoneId())
            .truncatedTo(ChronoUnit.MICROS);
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            StandardCharsets.UTF_8
    );

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private IShortenedUrlRepository shortenedUrlRepository;
    @Autowired
    private MockMvc mockMvc;
    private Long createdUrlId;

    @BeforeEach
    void setUp() {
        AuthUser user = new AuthUser("ForestWizard", "PassWord1234", Role.USER);
        userRepository.save(user);
        createdUrlId = shortenedUrlRepository.save(createShortUrl(user, 60)).getId();
    }

    @AfterEach
    void cleanUp() {
        shortenedUrlRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testCreateNewUrl() throws Exception {
        UrlCreateRequest request = new UrlCreateRequest(TEST_EXAMPLE_ORIGINAL_URL, 60);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/links")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request))
        ).andExpect(status().isCreated()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        UrlResponse response = objectMapper.readValue(json, UrlResponse.class);
        assertEquals(Status.OK, response.getError());
        assertEquals("ForestWizard", response.getCreatedBy());
        assertNotNull(response.getUrl());
        assertNotNull(response.getUrl().getId());
        assertEquals(TEST_EXAMPLE_ORIGINAL_URL, response.getUrl().getOriginalUrl());
        assertNotNull(response.getUrl().getShortUrl());
        assertTrue(response.getUrl().getShortUrl().startsWith("http://localhost:8080/"));
        assertNotNull(response.getUrl().getCreatedAt());
        assertEquals(response.getUrl().getCreatedAt(), response.getUrl().getExpiresAt().minusMinutes(60));
        assertEquals(0L, response.getUrl().getVisitTimes());
        createdUrlId = response.getUrl().getId();
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testGetAllUrls() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/links")
                .contentType(APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        UrlListResponse response = objectMapper.readValue(json, UrlListResponse.class);

        assertEquals(Status.OK, response.getError());
        assertEquals("ForestWizard", response.getCreatedBy());
        assertFalse(response.getUrls().isEmpty());
        assertNotNull(response.getUrls().getFirst());

        UrlDTO url = response.getUrls().getFirst();
        assertNotNull(url.getId());
        assertEquals(TEST_EXAMPLE_ORIGINAL_URL, url.getOriginalUrl());
        assertNotNull(url.getShortUrl());
        assertTrue(url.getShortUrl().startsWith("http://localhost:8080/"));
        assertEquals(TEST_EXAMPLE_CREATE_DATE, url.getCreatedAt());
        assertEquals(url.getCreatedAt(), url.getExpiresAt().minusMinutes(60));
        assertEquals(0L, url.getVisitTimes());
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testGetUrlByUsernameAndId() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/links/" + createdUrlId)
                .contentType(APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        UrlResponse response = objectMapper.readValue(json, UrlResponse.class);
        assertEquals(Status.OK, response.getError());
        assertEquals("ForestWizard", response.getCreatedBy());
        assertNotNull(response.getUrl());
        assertNotNull(response.getUrl().getId());
        assertEquals(TEST_EXAMPLE_ORIGINAL_URL, response.getUrl().getOriginalUrl());
        assertNotNull(response.getUrl().getShortUrl());
        assertTrue(response.getUrl().getShortUrl().startsWith("http://localhost:8080/"));
        assertEquals(TEST_EXAMPLE_CREATE_DATE, response.getUrl().getCreatedAt());
        assertEquals(response.getUrl().getCreatedAt(), response.getUrl().getExpiresAt().minusMinutes(60));
        assertEquals(0L, response.getUrl().getVisitTimes());
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testUpdateUrl() throws Exception {
        UrlCreateRequest request = new UrlCreateRequest(TEST_EXAMPLE_NEW_ORIGINAL_URL, 240);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/account/links/" + createdUrlId)
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request))
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        UrlResponse response = objectMapper.readValue(json, UrlResponse.class);
        assertEquals(Status.OK, response.getError());
        assertEquals("ForestWizard", response.getCreatedBy());
        assertNotNull(response.getUrl());
        assertNotNull(response.getUrl().getId());
        assertEquals(TEST_EXAMPLE_NEW_ORIGINAL_URL, response.getUrl().getOriginalUrl());
        assertNotNull(response.getUrl().getShortUrl());
        assertTrue(response.getUrl().getShortUrl().startsWith("http://localhost:8080/"));
        assertEquals(TEST_EXAMPLE_CREATE_DATE, response.getUrl().getCreatedAt());
        assertEquals(response.getUrl().getCreatedAt(), response.getUrl().getExpiresAt().minusMinutes(240));
        assertEquals(0L, response.getUrl().getVisitTimes());
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testDeleteUrl() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/account/links/" + createdUrlId)
                .contentType(APPLICATION_JSON_UTF8)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        StatusResponse response = objectMapper.readValue(json, StatusResponse.class);
        assertEquals(Status.OK, response.getError());

    }


    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testThrowsOnInvalidUrlCreateRequest() throws Exception {
        UrlCreateRequest request = new UrlCreateRequest("random_url", -1);
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/account/links")
                .contentType(APPLICATION_JSON_UTF8)
                .content(objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request))
        ).andExpect(status().isBadRequest()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        StatusResponse response = objectMapper.readValue(json, StatusResponse.class);
        assertEquals(Status.INVALID_ORIGINAL_URL_FORMAT, response.getError());
    }

    @Test
    @WithMockUser(username = "ForestWizard", roles = "USER")
    void testThrowsOnInvalidUrlId() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/account/links/123")
                .contentType(APPLICATION_JSON_UTF8)
        ).andExpect(status().isNotFound()).andExpect(content().contentType("application/json")).andReturn();
        String json = result.getResponse().getContentAsString();
        StatusResponse response = objectMapper.readValue(json, StatusResponse.class);
        assertEquals(Status.SUCH_URL_NOT_EXISTS, response.getError());
    }

    private ShortenedUrl createShortUrl(AuthUser user, int expireTime) {
        return ShortenedUrl.builder()
                .user(user)
                .originalUrl(TEST_EXAMPLE_ORIGINAL_URL)
                .shortUrl(TEST_EXAMPLE_SHORT_URL)
                .createdAt(TEST_EXAMPLE_CREATE_DATE)
                .expiresAt(TEST_EXAMPLE_CREATE_DATE.plusMinutes(expireTime))
                .visitTimes(0L)
                .build();
    }
}
