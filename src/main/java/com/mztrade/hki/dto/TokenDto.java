package com.mztrade.hki.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;
}
