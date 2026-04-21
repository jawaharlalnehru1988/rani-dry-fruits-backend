package com.asknehru.fruitsapi.config;

import com.asknehru.fruitsapi.exception.UnauthorizedException;
import com.asknehru.fruitsapi.service.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;

    public AdminAuthInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestPath = request.getRequestURI();

        if (requestPath.startsWith("/api/admin/auth/")) {
            return true;
        }

        String method = request.getMethod();
        if (HttpMethod.GET.matches(method) || HttpMethod.HEAD.matches(method) || HttpMethod.OPTIONS.matches(method)) {
            return true;
        }

        String token = extractToken(request.getHeader("Authorization"));
        String username = adminAuthService.validateAndGetUsername(token);

        if (username == null) {
            throw new UnauthorizedException("Admin login required");
        }

        return true;
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
