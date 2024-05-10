package com.mztrade.hki.config;

import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(1)
public class FirebaseTokenFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenFilter(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        System.out.println(request.getRequestURI());
        for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
            String header = it.next();
        }
        chain.doFilter(request, response);
        /*if (request.getRequestURI().startsWith("/user/duplicate-check")) {
            chain.doFilter(request, response);
        } else if (request.getRequestURI().startsWith("/send-email")) {
            chain.doFilter(request, response);
        } else if (request.getRequestURI().startsWith("/check-email")) {
            chain.doFilter(request, response);
        } else if (request.getRequestURI().startsWith("/login")) {
            chain.doFilter(request, response);
        } else {
            try {
                System.out.println(request.getHeader("Authorization"));
                firebaseAuth.verifyIdToken(request.getHeader("Authorization"));
                chain.doFilter(request, response);
            } catch (IllegalArgumentException e) {
                System.out.println("토큰이 없음");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "토큰이 없거나 잘못된 형식입니다.");
            } catch (FirebaseAuthException e) {
                System.out.println("토큰 검증 실패");
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다.");
            }
        }*/
    }
}