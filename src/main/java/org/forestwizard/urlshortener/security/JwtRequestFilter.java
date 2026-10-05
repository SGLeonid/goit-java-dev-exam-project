package org.forestwizard.urlshortener.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.forestwizard.urlshortener.status.StatusResponse;
import org.forestwizard.urlshortener.status.Status;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JwtRequestFilter extends BasicAuthenticationFilter {
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_AUTHORIZATION_BEGIN = "Bearer ";

    UserDetailsService userDetailsService;
    JwtService jwtService;

    public JwtRequestFilter(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtService jwtService
    ) {
        super(authenticationManager);
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String header = request.getHeader(HEADER_AUTHORIZATION);
        String username;
        String jwt;

        try {
            if (header != null && header.startsWith(HEADER_AUTHORIZATION_BEGIN)) {
                jwt = header.substring(7);
                username = jwtService.extractUsername(jwt);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (jwtService.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(token);
                    }
                }
            }
        } catch (ExpiredJwtException _) {
            setResponse(response, Status.SESSION_EXPIRED);
            return;
        } catch (MalformedJwtException _) {
            setResponse(response, Status.SESSION_MALFORMED);
            return;
        } catch (SignatureException _) {
            setResponse(response, Status.SESSION_INTERNAL_ERROR);
            return;
        }

        chain.doFilter(request, response);
    }

    private void setResponse(HttpServletResponse response, Status status) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new StatusResponse(status));
        response.setStatus(401);
        response.setContentType("application/json");
        response.getWriter().println(json);
        response.getWriter().close();
    }
}