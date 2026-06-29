package com.orderflow.order.dto;

public record UserProfile(
        Long id,
        String email,
        String firstName,
        String lastName
){}
