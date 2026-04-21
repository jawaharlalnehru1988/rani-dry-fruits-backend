package com.asknehru.fruitsapi.dto;

import java.time.Instant;

public record AdminLoginResponse(
    boolean success,
    String message,
    String token,
    String username,
    Instant expiresAt
) {
}
