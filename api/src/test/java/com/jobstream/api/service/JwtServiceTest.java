package com.jobstream.api.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "dGVzdHNlY3JldGtleXRlc3RzZWNyZXRrZXl0ZXN0c2VjcmV0a2V5MTIzNA==";
    private static final String OTHER_SECRET = "YXV0aGVyc2VjcmV0a2V5YXV0aGVyc2VjcmV0a2V5YXV0aGVyc2VjcmV0MTI=";
    private static final long EXPIRATION = 3600000L; // 1h

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    private User user(String username) {
        return new User(username, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void generateToken_andExtractUsername_shouldRoundTrip() {
        User user = user("alice@test.com");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@test.com");
    }

    @Test
    void isTokenValid_shouldReturnTrue_forValidTokenAndMatchingUser() {
        User user = user("bob@test.com");
        String token = jwtService.generateToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenUsernameMismatch() {
        User owner = user("alice@test.com");
        User other = user("bob@test.com");
        String token = jwtService.generateToken(owner);

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_shouldThrow_whenExpired() {
        JwtService expiredService = new JwtService(SECRET, -1000L);
        User user = user("alice@test.com");
        // token with expiration in the past -> parsing throws ExpiredJwtException
        String token = expiredService.generateToken(user);

        assertThatThrownBy(() -> expiredService.isTokenValid(token, user))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_shouldThrow_whenSignatureInvalid() {
        User user = user("alice@test.com");
        JwtService otherService = new JwtService(OTHER_SECRET, EXPIRATION);
        String tokenSignedWithOtherKey = otherService.generateToken(user);

        assertThatThrownBy(() -> jwtService.extractUsername(tokenSignedWithOtherKey))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_shouldThrow_whenTokenMalformed() {
        assertThatThrownBy(() -> jwtService.extractUsername("not.a.jwt"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_shouldThrow_whenTokenTampered() {
        User user = user("alice@test.com");
        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + "ab";

        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateToken_shouldSetExpirationInFuture() {
        User user = user("alice@test.com");
        String token = jwtService.generateToken(user);

        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void getSigningKey_shouldThrow_whenSecretNotBase64() {
        JwtService badService = new JwtService("not-base64!!!", EXPIRATION);
        User user = user("alice@test.com");

        assertThatThrownBy(() -> badService.generateToken(user))
                .isInstanceOf(Exception.class);
    }

    @Test
    void isTokenValid_shouldThrow_whenTokenExpired_differentService() {
        JwtService shortLived = new JwtService(SECRET, -1000L);
        User user = user("alice@test.com");
        String expiredToken = shortLived.generateToken(user);

        // Parsing with the regular service should also throw because token is already expired
        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, user))
                .isInstanceOf(Exception.class);
    }
}
