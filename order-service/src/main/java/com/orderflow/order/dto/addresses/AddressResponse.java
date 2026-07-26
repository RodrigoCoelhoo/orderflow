package com.orderflow.order.dto.addresses;

import com.orderflow.order.model.Address;

public record AddressResponse(
        Long id,
        String label,
        String street,
        String city,
        String postalCode,
        String country,
        boolean isDefault
) {
    public static AddressResponse toDto(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getLabel(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault()
        );
    }
}