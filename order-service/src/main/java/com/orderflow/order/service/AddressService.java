package com.orderflow.order.service;

import com.orderflow.order.dto.addresses.AddressRequest;
import com.orderflow.order.dto.addresses.AddressResponse;
import com.orderflow.order.exceptions.ForbiddenException;
import com.orderflow.order.exceptions.ResourceNotFound;
import com.orderflow.order.model.Address;
import com.orderflow.order.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddressesByUser(
            Long userId
    ) {
        return addressRepository.findByUserId(userId).stream()
                .map(AddressResponse::toDto)
                .toList();
    }

    @Transactional
    public AddressResponse createAddress(
            AddressRequest request,
            Long userId
    ) {
        if (request.isDefault()) {
            addressRepository.unsetDefaultForUser(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .label(request.label())
                .street(request.street())
                .city(request.city())
                .postalCode(request.postalCode())
                .country(request.country())
                .isDefault(request.isDefault())
                .build();

        return AddressResponse.toDto(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse updateAddress(
            Long id,
            AddressRequest request,
            Long userId
    ) {
        Address address = getOwnedAddress(id, userId);

        if (request.isDefault() && !address.isDefault()) {
            addressRepository.unsetDefaultForUser(userId);
        }

        address.setLabel(request.label());
        address.setStreet(request.street());
        address.setCity(request.city());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setDefault(request.isDefault());

        return AddressResponse.toDto(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(
            Long id,
            Long userId
    ) {
        Address address = getOwnedAddress(id, userId);
        addressRepository.delete(address);
    }

    @Transactional(readOnly = true)
    public Address getOwnedAddress(
            Long id,
            Long userId
    ) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Address not found"));

        if (!address.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this address");
        }

        return address;
    }
}
