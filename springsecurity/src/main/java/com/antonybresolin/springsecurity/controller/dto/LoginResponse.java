package com.antonybresolin.springsecurity.controller.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
