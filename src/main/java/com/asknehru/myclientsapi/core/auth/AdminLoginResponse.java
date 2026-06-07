package com.asknehru.myclientsapi.core.auth;

import java.time.Instant;

public record AdminLoginResponse(
    boolean success,
    String message,
    String token,
    String username,
    Instant expiresAt
) {
}
