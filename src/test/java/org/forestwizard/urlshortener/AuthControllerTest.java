package org.forestwizard.urlshortener;

import org.forestwizard.urlshortener.auth.*;
import org.forestwizard.urlshortener.exception.GlobalExceptionHandler;
import org.forestwizard.urlshortener.status.Status;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class AuthControllerTest {
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
    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(GlobalExceptionHandler.class)
                .build();
    }

    @AfterEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterAndLogin() throws Exception {
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1234");
        String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);

        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
        ).andExpect(status().isCreated()).andExpect(content().contentType("application/json")).andReturn();
        String registerResponseJson = registerResult.getResponse().getContentAsString();
        AuthResponse registerResponse = objectMapper.readValue(registerResponseJson, AuthResponse.class);
        assertEquals(Status.OK, registerResponse.getError());
        assertFalse(() -> registerResponse.getToken().trim().isEmpty());

        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
        ).andExpect(status().isOk()).andExpect(content().contentType("application/json")).andReturn();
        String loginResponseJson = loginResult.getResponse().getContentAsString();
        AuthResponse loginResponse = objectMapper.readValue(loginResponseJson, AuthResponse.class);
        assertEquals(Status.OK, loginResponse.getError());
        assertFalse(() -> loginResponse.getToken().trim().isEmpty());
    }

    @Test
    void testThrowsOnInvalidAuthRequest() throws Exception {
        AuthRequest request = new AuthRequest("ForestWizard", "password");
        String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        MvcResult result;
        String responseJson;
        StatusResponse registerResponse;

        result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register").contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andReturn();
        responseJson = result.getResponse().getContentAsString();
        registerResponse = objectMapper.readValue(responseJson, StatusResponse.class);
        assertEquals(Status.REQUEST_BODY_BAD_OR_MISSING, registerResponse.getError());

        result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
        ).andExpect(status().isBadRequest()).andExpect(content().contentType("application/json")).andReturn();
        responseJson = result.getResponse().getContentAsString();
        registerResponse = objectMapper.readValue(responseJson, StatusResponse.class);
        assertEquals(Status.PASSWORD_REQUIREMENTS_UNSATISFIED, registerResponse.getError());
    }

    @Test
    void testThrowsOnAlreadyExistingUser() throws Exception {
        userRepository.save(new AuthUser("ForestWizard", "PassWord1234", Role.USER));
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1234");
        String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/register")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
        ).andExpect(status().isConflict()).andExpect(content().contentType("application/json")).andReturn();
        String registerResponseJson = registerResult.getResponse().getContentAsString();
        StatusResponse registerResponse = objectMapper.readValue(registerResponseJson, StatusResponse.class);
        assertEquals(Status.SUCH_USER_ALREADY_EXISTS, registerResponse.getError());
    }

    @Test
    void testThrowsOnInvalidPassword() throws Exception {
        userRepository.save(new AuthUser("ForestWizard", "PassWord1234", Role.USER));
        AuthRequest request = new AuthRequest("ForestWizard", "PassWord1111");
        String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(request);
        MvcResult registerResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON_UTF8)
                .content(json)
        ).andExpect(status().isUnauthorized()).andExpect(content().contentType("application/json")).andReturn();
        String registerResponseJson = registerResult.getResponse().getContentAsString();
        StatusResponse registerResponse = objectMapper.readValue(registerResponseJson, StatusResponse.class);
        assertEquals(Status.INVALID_USERNAME_OR_PASSWORD, registerResponse.getError());
    }
}
