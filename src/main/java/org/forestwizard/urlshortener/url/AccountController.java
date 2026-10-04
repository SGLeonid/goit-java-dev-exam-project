package org.forestwizard.urlshortener.url;

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

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "Short URL management", description = "End points for managing authenticated account's short URLs")
public class AccountController {
    private final UrlService urlService;

    @Operation(
            summary = "Get list of all URLs of current account",
            description = "Returns a list of all URLs created by this account or empty list if there are no such URLs."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully returned list of current account's URLs",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlListResponse.class))
    )
    @GetMapping("/links")
    public ResponseEntity<UrlListResponse> getUrlListByUsername(Principal principal) {
        UrlListResponse response = urlService.getAllByUsername(principal.getName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Returns a URL info with specified ID of current account",
            description = "Returns a URL made by this account with specified ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully returned current account's URL info",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Short URL with such ID not found", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"SUCH_URL_NOT_EXISTS\"}")
    ))
    @GetMapping("/links/{id}")
    public ResponseEntity<UrlResponse> getUrlByUsernameAndId(Principal principal, @PathVariable("id") Long id) {
        UrlResponse response = urlService.getByUsernameAndId(principal.getName(), id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Creates a new URL for current account",
            description = "Creates a new account's URL with specified original URL and expiration time in minutes."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Successfully created a new URL by current account",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid original url or expiration time", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"INVALID_ORIGINAL_URL_FORMAT\"}")
    ))
    @PostMapping("/links")
    public ResponseEntity<UrlResponse> postUrl(Principal principal, @RequestBody UrlCreateRequest request) {
        UrlResponse response = urlService.create(principal.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Edits a URL of current account",
            description = "Edits a URL of current account by specifying original URL and expiration time in minutes."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully edited URL by current account",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))
    )
    @ApiResponse(responseCode = "400", description = "Invalid original url or expiration time", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"INVALID_URL_EXPIRATION_TIME\"}")
    ))
    @ApiResponse(responseCode = "404", description = "Short URL with such ID not found", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"SUCH_URL_NOT_EXISTS\"}")
    ))
    @PatchMapping("/links/{id}")
    public ResponseEntity<UrlResponse> pathUrl(
            Principal principal,
            @PathVariable("id") Long id,
            @RequestBody UrlCreateRequest request
    ) {
        UrlResponse response = urlService.update(principal.getName(), id, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "Deletes a URL of current account",
            description = "Deletes a URL made by this account by it's ID if such exists."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Successfully deleted URL of current account",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UrlResponse.class))
    )
    @ApiResponse(responseCode = "404", description = "Short URL with such ID not found", content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = StatusResponse.class),
            examples = @ExampleObject(value = "{\"error\":\"SUCH_URL_NOT_EXISTS\"}")
    ))
    @DeleteMapping("/links/{id}")
    public ResponseEntity<StatusResponse> deleteUrlByUsernameAndId(Principal principal, @PathVariable("id") Long id) {
        StatusResponse response = urlService.deleteByUsernameAndId(principal.getName(), id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
