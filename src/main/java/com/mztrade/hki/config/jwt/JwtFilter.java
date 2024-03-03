package com.mztrade.hki.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;


@Slf4j
public class JwtFilter extends GenericFilterBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider tokenProvider;

    public JwtFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     *  1) 필터링 로직 정의
     *  2) 토큰의 인증정보(Authenticate 객체)를 SecurityContext 에 저장하는 메서드
     *  3) 여길 통과해야 url 접근 가능
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        // Header 에서 토큰을 받아옴
        String accessToken = resolveToken(httpServletRequest);

        // 현재 요청의 URI를 받아옴
        String requestURI = httpServletRequest.getRequestURI();

        log.info("tokenProvider.validateToken(accessToken) : {}", tokenProvider.validateToken(accessToken));
        log.info("StringUtils.hasText(accessToken) : {}", StringUtils.hasText(accessToken));

        // 받아온 jwt 토큰을 validateToken 메서드로 유효성 검증
        if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {

            // 토큰이 정상이라면 Authentication 객체를 받아옴
            Authentication authentication = tokenProvider.getAuthentication(accessToken);

            // 정상 토큰이면 SecurityContext에 Authentication 객체를 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.info("Security Context에 '{}' 인증정보를 저장했습니다., uri: {}", authentication.getName(), requestURI);

        } else {
            log.info("유효한 JWT 토큰이 없습니다, uri : {}", requestURI);
        }


        filterChain.doFilter(servletRequest, servletResponse);

    }

    // Request Header 에서 토큰 정보를 꺼내오기 위한 resolveToken 메서드 정의
    private String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        log.info("bearerToken : {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
