package org.forestwizard.urlshortener.exception;

import org.forestwizard.urlshortener.status.StatusResponse;
import org.forestwizard.urlshortener.status.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.view.RedirectView;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StatusResponse> handle(HttpMessageNotReadableException e) {
        return new ResponseEntity<>(
                new StatusResponse(Status.REQUEST_BODY_BAD_OR_MISSING),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<StatusResponse> handle(InvalidRequestException e) {
        return new ResponseEntity<>(new StatusResponse(e.getStatus()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StatusResponse> handle(AuthenticationException e) {
        return new ResponseEntity<>(new StatusResponse(e.getStatus()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RegisterException.class)
    public ResponseEntity<StatusResponse> handle(RegisterException e) {
        return new ResponseEntity<>(new StatusResponse(e.getStatus()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<StatusResponse> handle(UrlNotFoundException e) {
        return new ResponseEntity<>(new StatusResponse(e.getStatus()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<StatusResponse> handle(UsernameNotFoundException e) {
        return new ResponseEntity<>(new StatusResponse(e.getStatus()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RedirectUrlExpiredException.class)
    public RedirectView handle(RedirectUrlExpiredException e) {
        return new RedirectView("/expired");
    }

    @ExceptionHandler(RedirectUrlNotFoundException.class)
    public RedirectView handle(RedirectUrlNotFoundException e) {
        return new RedirectView("/notfound");
    }
}
