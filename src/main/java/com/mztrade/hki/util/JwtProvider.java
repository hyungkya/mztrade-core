package com.mztrade.hki.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey key;
    byte[] keyBytes;

    @PostConstruct
    protected void init(){
        keyBytes = Base64.getEncoder().encode(secretKey.getBytes());
        key = Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * @param username
     * @return String 토큰 생성하기 (JWT의 Payloady 부분 정의)
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1800000)) // 30분 유효시간 설정
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }

    /**
     *
     * @param token
     * @param username
     * @return boolean 토큰 검증
     */
    public boolean validateToken(String token, String username){
        final String tokenUsername = extractUsername(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * @param token
     * @return String : Extract Username from token
     */
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * @param token
     * @return Date : Extract Expiration from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * @param token
     * @param claimsResolver
     * @return 선언된 함수 결과를 반환(Ex.String or Date)
     */
    public <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        // <T> : 제네릭 타입 T를 사용하겠다고 선언
        // T : 실제 메스드의 반환 타입, 메서드 실행시 결정됨
        final Claims claims = extracAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * @param token
     * @return Claims : All claims are returned
     */
    private Claims extracAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }


    /**
     * @param token
     * @return boolean : check the expiration of token
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}
