package com.asknehru.fruitsapi.controller;

import com.asknehru.fruitsapi.dto.AdminLoginRequest;
import com.asknehru.fruitsapi.dto.AdminLoginResponse;
import com.asknehru.fruitsapi.exception.UnauthorizedException;
import com.asknehru.fruitsapi.service.AdminAuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        adminAuthService.logout(extractToken(authorizationHeader));
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String username = adminAuthService.validateAndGetUsername(extractToken(authorizationHeader));
        if (username == null) {
            throw new UnauthorizedException("Invalid or expired admin token");
        }
        return ResponseEntity.ok(Map.of("username", username));
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring("Bearer ".length()).trim();
        }

        return authorizationHeader.trim();
    }
}
