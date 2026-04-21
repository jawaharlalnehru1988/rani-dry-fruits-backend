package com.asknehru.fruitsapi.service;

import com.asknehru.fruitsapi.dto.AdminLoginRequest;
import com.asknehru.fruitsapi.dto.AdminLoginResponse;
import com.asknehru.fruitsapi.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final String configuredUsername;
    private final String configuredPassword;
    private final long sessionTtlMinutes;
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public AdminAuthService(
        @Value("${admin.auth.username}") String configuredUsername,
        @Value("${admin.auth.password}") String configuredPassword,
        @Value("${admin.auth.session-ttl-minutes}") long sessionTtlMinutes
    ) {
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!configuredUsername.equals(request.username()) || !configuredPassword.equals(request.password())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        Instant expiresAt = Instant.now().plusSeconds(sessionTtlMinutes * 60);
        sessions.put(token, new SessionData(request.username(), expiresAt));

        return new AdminLoginResponse(
            true,
            "Login successful",
            token,
            request.username(),
            expiresAt
        );
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public String validateAndGetUsername(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        SessionData data = sessions.get(token);
        if (data == null) {
            return null;
        }

        if (data.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }

        return data.username();
    }

    private record SessionData(String username, Instant expiresAt) {
    }
}
