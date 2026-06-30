package com.orderflow.order.dto.auth;

public record UserProfile(
        Long id,
        String email,
        String firstName,
        String lastName
){}
