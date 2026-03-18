package com.ou.springcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "defaultSecMustBeLongEnoughForHS256";
    private Long accessTokenExpiresMs = 900_000L;  // 15 * 60 * 1000
    private Long refreshTokenExpiresMs = 640_800_000L;  // 7 *24 *60 * 60 * 1000 

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getAccessTokenExpiresMs() {
        return accessTokenExpiresMs;
    }

    public void setAccessTokenExpiresMs(Long accessTokenExpiresMs) {
        this.accessTokenExpiresMs = accessTokenExpiresMs;
    }

    public Long getRefreshTokenExpiresMs() {
        return refreshTokenExpiresMs;
    }

    public void setRefreshTokenExpiresMs(Long refreshTokenExpiresMs) {
        this.refreshTokenExpiresMs = refreshTokenExpiresMs;
    }

}
