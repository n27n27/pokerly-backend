package com.rolling.pokerly.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-validity-seconds}")
    private long accessValiditySec;

    @Value("${jwt.refresh-token-validity-seconds}")
    private long refreshValiditySec;

    private SecretKey key;

    @PostConstruct
    @SuppressWarnings("unused")
    void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String nickname, String role) {
        var now = new Date();
        var expiry = new Date(now.getTime() + accessValiditySec * 1000);
        return Jwts.builder()
                .subject(nickname)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(String nickname) {
        var now = new Date();
        var expiry = new Date(now.getTime() + refreshValiditySec * 1000);
        return Jwts.builder()
                .subject(nickname)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        var userId = claims.get("userId", Long.class);
        var nickname = claims.getSubject();
        var role = (String) claims.get("role");
        var authorities = List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role));
        var principal = new CustomPrincipal(userId, nickname, authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public LocalDateTime extractExpiry(String token) {
        var claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
        return LocalDateTime.ofInstant(claims.getExpiration().toInstant(), ZoneId.systemDefault());
    }

    public String extractSubject(String token) {
        var claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

}
