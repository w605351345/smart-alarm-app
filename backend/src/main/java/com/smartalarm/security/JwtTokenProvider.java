package com.smartalarm.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:your-secret-key-must-be-at-least-32-characters-long-for-security}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long tokenValidityInMilliseconds;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenValidityInMilliseconds;

    private Key key;

    @PostConstruct
    protected void init() {
        // 确保密钥长度至少 32 字节
        byte[] keyBytes = secretKey.getBytes();
        if (keyBytes.length < 32) {
            log.warn("JWT secret key is too short, using padded key");
            secretKey = String.format("%-64s", secretKey).replace(' ', 'x');
        }
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * 生成访问令牌
     */
    public String createToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 生成访问令牌（自定义 claims）
     */
    public String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    public String createRefreshToken(String subject) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDate(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取 claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 获取所有 claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + tokenValidityInMilliseconds));
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to refresh token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查令牌是否可刷新
     */
    public boolean canTokenBeRefreshed(String token) {
        try {
            final Date expiration = getExpirationDate(token);
            return expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
