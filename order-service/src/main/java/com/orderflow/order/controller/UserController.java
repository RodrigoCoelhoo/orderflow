package com.orderflow.order.controller;

import com.orderflow.order.service.UserService;
import com.orderflow.order.dto.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfile> me(@AuthenticationPrincipal UserProfile user) {
        log.debug("GET /api/users/me [userId={}]", user.id());
        return ResponseEntity.ok(user);
    }
}
