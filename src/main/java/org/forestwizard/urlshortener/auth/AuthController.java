package org.forestwizard.urlshortener.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "Register and login end points for authorized API requests")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user if such does not exist. When creating a new user, the password must " +
                    "contain digits, upper and lower case letters and be at least 8 symbols long."
    )
    @ApiResponse(responseCode = "201", description = "Successfully created a new user", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AuthResponse.class)
    ))
    @ApiResponse(responseCode = "400", description = "Invalid register request", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"INVALID_PASSWORD_LENGTH\"}")
    ))
    @ApiResponse(responseCode = "403", description = "Such user already exists", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"SUCH_USER_ALREADY_EXISTS\"}")
    ))
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Authenticates a user",
            description = "Authenticates a new user if such does not exist. When logging in, the password must " +
                    "contain digits, upper and lower case letters and be at least 8 symbols long."
    )
    @ApiResponse(responseCode = "200", description = "Successfully authenticated user", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = AuthResponse.class)
    ))
    @ApiResponse(responseCode = "400", description = "Invalid authentication request", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"INVALID_USERNAME_LENGTH\"}")
    ))
    @ApiResponse(responseCode = "403", description = "Invalid username or password", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"INVALID_PASSWORD\"}")
    ))
    @ApiResponse(responseCode = "404", description = "Such user is not found", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"SUCH_USER_NOT_EXISTS\"}")
    ))
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
