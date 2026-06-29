package com.orderflow.order.controller;

import com.orderflow.order.model.User;
import com.orderflow.order.dto.AuthResponse;
import com.orderflow.order.service.AuthenticationService;
import com.orderflow.order.service.UserService;
import com.orderflow.order.dto.CreateUserRequest;
import com.orderflow.order.dto.SignInRequest;
import com.orderflow.order.dto.UserProfile;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<UserProfile> signup(
            @RequestBody @Valid CreateUserRequest request
    ) {
        log.debug(
                "POST /api/auth/signup [email={}]",
                request.email()
        );

        UserProfile response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(
            @RequestBody @Valid SignInRequest request,
            HttpServletResponse response
    ) {
        log.debug(
                "POST /api/auth/signin [email={}]",
                request.email()
        );

        User user = userService.getUserByEmail(request.email());

        try {
            authenticationService.checkPassword(
                    request.password(),
                    user.getPassword()
            );
        } catch (BadCredentialsException e) {
            log.warn("Authentication failed [email={}]", request.email());
            throw e;
        }

        AuthResponse token = authenticationService.generateAuthToken(user, response, "Strict");
        return ResponseEntity.ok(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(
            HttpServletResponse response
    ) {
        authenticationService.logout(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        AuthResponse response = authenticationService.refresh(refreshToken);
        return ResponseEntity.ok(response);
    }
}
