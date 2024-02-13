package com.mztrade.hki.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;


/**
 * InitializingBean 을 implements 하고, afterPropertiesSet을 override한 이유
 * 1) Bean 이 생성이 되고(Component Scan),
 * 2) 의존성 주입(생성자 주입)을 받은 후
 * 3) 주입받은 secret 값을 Base64 Decode해서 key 변수에 할당위함
 */
@Component
@Slf4j
public class TokenProvider implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(TokenProvider.class);

    private static final String AUTHORITIES_KEY = "auth";

    private final String secret;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final RedisTemplate<String, String> redisTemplate;
    private Key key;

    // 2)
    public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration-time}") long accessExpiration,
            @Value("${jwt.refresh-expiration-time}") long refreshExpiration,
            RedisTemplate<String, String> redisTemplate) {
        this.secret = secret;
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.redisTemplate = redisTemplate;
    }

    // 3) 생성자 주입 및 빈 생성 이후 aferPropertiesSet 함수 실행(InitializingBean 상속받은 이유)
    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // Authentication 객체의 권한정보를 이용해 토큰을 생성하는 createToken 메서드 추가
    public String createAccessToken(Authentication authentication) {

        // authentication 권한 초기화
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

        // Access Token 생성
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(new Date(now + this.accessExpiration))
                .compact();
    }

    // Refresh Token 생성
    public String createRefreshToken(Authentication authentication) {
        Claims claims = Jwts.claims().setSubject(authentication.getName());

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + this.refreshExpiration);

        String refreshToken = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Redis에 Refresh Token 저장
        redisTemplate.opsForValue().set(authentication.getName(), refreshToken, refreshExpiration, TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    /**
     * Token에 담긴 정보를 이용해 Authentication 객체를 리턴
     */
    public Authentication getAuthentication(String token) {

        // 파라미터로 받아온 token을 이용해서 Claim 생성
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Claim에서 권한정보들을 빼냄
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

        // 빼낸 권한정보를 이용해 유저를 생성
        User principal = new User(claims.getSubject(), "", authorities);

        // 유저정보, 토큰, 권한정보를 가진 Authentication 객체 리턴
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);

    }


    /**
     * Access Token 유효성 검증
     */
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e){
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e){
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e){
            logger.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }
}
