package com.ou.springcode.dto;

public record AuthReponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresInSecond,
    UserReponse user
) {
    public AuthReponse {
        if(tokenType == null || tokenType.isBlank()) {
            tokenType = "Bearer";
        }
    }

    public static AuthReponse of(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresInSecond,
        UserReponse user
    ) {
        return new AuthReponse(accessToken, refreshToken, tokenType, expiresInSecond, user);
    }
}
