package com.mztrade.hki.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;


/**
 * Token 정보를 Response 할 때 사용
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
