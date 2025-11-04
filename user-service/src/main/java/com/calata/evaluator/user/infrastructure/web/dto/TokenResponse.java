package com.calata.evaluator.user.infrastructure.web.dto;

public class TokenResponse {

    public String accessToken;

    public TokenResponse(String accessToken){
        this.accessToken = accessToken;
    }
}
