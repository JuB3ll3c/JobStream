package com.jobstream.api.config;

import com.jobstream.api.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        // 1. If no Header or not beginning with "Bearer ", pass to next filter
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Extract username from token
        final String jwt = authHeader.substring(7);
        final String userEmail;
        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException exception) {
            rejectAuthentication(request, response, exception);
            return;
        }

        // 3. If the user is present and not authenticate in the spring context
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            final UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            } catch (UsernameNotFoundException exception) {
                rejectAuthentication(request, response, exception);
                return;
            }

            final boolean tokenValid;
            try {
                tokenValid = jwtService.isTokenValid(jwt, userDetails);
            } catch (JwtException | IllegalArgumentException exception) {
                rejectAuthentication(request, response, exception);
                return;
            }

            // 4. If the token is valid, create the authentication object
            if (tokenValid) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Inject the authentication object in the spring context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Request continue to the controller
        filterChain.doFilter(request, response);
    }

    private void rejectAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            RuntimeException exception
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(
                request,
                response,
                new BadCredentialsException("Invalid or expired token", exception)
        );
    }
}
