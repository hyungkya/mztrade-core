package com.mztrade.hki.controller;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.mztrade.hki.config.jwt.JwtFilter;
import com.mztrade.hki.config.jwt.TokenProvider;
import com.mztrade.hki.dto.DefaultResponse;
import com.mztrade.hki.dto.LoginRequestDto;
import com.mztrade.hki.dto.LoginResponseDto;
import com.mztrade.hki.dto.ResponseMessage;
import com.mztrade.hki.dto.StatusCode;
import com.mztrade.hki.dto.TokenDto;
import com.mztrade.hki.service.RedisService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisService redisService;

    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam String idToken) throws FirebaseAuthException {
        System.out.println(idToken);
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        System.out.println("hi");
        return null;
    }

    @PostMapping("/auth/issue")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginRequestDto loginRequestDto) {

        log.info("토큰 발급 시작");

        // 로그인 유저정보를 이용하여 authenticationToken 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequestDto.getName(), loginRequestDto.getPassword());

        // authenticationToken 객체를 이용하여 authenticate 메서드를 호출하여 인증정보를 받아옴
        Authentication authentication = authenticationManagerBuilder.getObject()
                .authenticate(authenticationToken);

        // SecurityContext 에 인증정보를 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // accessToken, refreshToken 생성(refresh 토큰은 redis에 저장)
        String accessToken = tokenProvider.createAccessToken(authentication);
        String refreshToken = tokenProvider.createRefreshToken(authentication);


        HttpHeaders httpHeaders = new HttpHeaders();
        Map<String, Object> tokens = new HashMap<>();


        // 토큰을 Response Header 에 넣어서 반환
        httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + accessToken);
        httpHeaders.add("Refresh-Token", "Bearer " + refreshToken);

        tokens.put("access_token", accessToken);
        tokens.put("refresh_token", refreshToken);

        log.info("토큰 발급 완료");

        return new ResponseEntity<>(tokens, httpHeaders, HttpStatus.OK);


    }

    @PostMapping("/auth/reissue")
    public ResponseEntity<?> reissue(@Valid @RequestBody TokenDto tokenDto) {

        log.info("토큰 재발급 시작");
        try {
            // accessToken 유효성 검사
            boolean validateToken = tokenProvider.validateToken(tokenDto.getAccessToken());
            // access token 이 유효하지 않은 경우
            if (!validateToken) {

                String username = tokenProvider.getUsername(tokenDto.getRefreshToken());

                // Redis에서 저장된 Refresh Token 가져오기
                String storedRefreshToken = redisService.getRefreshToken(username);
                String refreshToken = tokenDto.getRefreshToken();

                if (storedRefreshToken != null && storedRefreshToken.equals(refreshToken)
                        && tokenProvider.validateToken(refreshToken)) {

                    // 새로운 Access Token 생성
                    String newAccessToken = tokenProvider.createAccessToken(username);

                    // 선택적으로 새로운 Refresh Token 생성 및 기존의 것 대체
                    String newRefreshToken = tokenProvider.createRefreshToken(username);

                    HttpHeaders httpHeaders = new HttpHeaders();

                    // 토큰을 Response Header 에 넣어서 반환
                    httpHeaders.add(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + newAccessToken);
                    httpHeaders.add("Refresh-Token", "Bearer " + newRefreshToken);

                    Map<String, Object> tokens = new HashMap<>();

                    tokens.put("access_token", newAccessToken);
                    tokens.put("refresh_token", newRefreshToken);

                    log.info("토큰 재발급 완료");
                    return new ResponseEntity<>(tokens, httpHeaders, HttpStatus.OK);


                }

            }
        } catch (Exception e) {

            log.info("토큰 재발급 실패");

            // 400 status code 반환
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return null;
    }
}
