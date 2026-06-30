package com.orderflow.order.service;

import com.orderflow.order.model.User;
import com.orderflow.order.exceptions.InvalidTokenException;
import com.orderflow.order.dto.auth.AuthResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserService userService;

    public void checkPassword(String password, String userPassword) {
        if(!passwordEncoder.matches(password, userPassword)) {
            throw new BadCredentialsException("Invalid Credentials");
        }
    }

    public AuthResponse generateAuthToken(User user, HttpServletResponse response, String sameSite) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // true in production (HTTPS)
                .path("/api/auth")
                .maxAge(30 * 24 * 60 * 60)
                .sameSite(sameSite)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return new AuthResponse(accessToken);
    }

    public AuthResponse refresh(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token missing");
        }

        String userId = tokenService.validateRefreshToken(refreshToken);
        User user = userService.getUserById(Long.parseLong(userId));
        String newAccessToken = tokenService.generateAccessToken(user);

        return new AuthResponse(newAccessToken);
    }

    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
