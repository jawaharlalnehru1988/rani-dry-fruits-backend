package com.asknehru.myclientsapi.core.auth;

import com.asknehru.myclientsapi.core.auth.AdminLoginRequest;
import com.asknehru.myclientsapi.core.auth.AdminLoginResponse;
import com.asknehru.myclientsapi.core.exception.UnauthorizedException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminUserRepository adminUserRepository;
    private final long sessionTtlMinutes;
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();

    public AdminAuthService(
        AdminUserRepository adminUserRepository,
        @Value("${admin.auth.session-ttl-minutes:120}") long sessionTtlMinutes
    ) {
        this.adminUserRepository = adminUserRepository;
        this.sessionTtlMinutes = sessionTtlMinutes;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.username())
            .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!admin.getPassword().equals(request.password())) {
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
