package com.orderflow.order.dto.addresses;

public record ShippingAddress(
        String street,
        String city,
        String postalCode,
        String country
) {}
