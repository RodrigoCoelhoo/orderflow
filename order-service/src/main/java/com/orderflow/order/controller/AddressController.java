package com.orderflow.order.controller;

import com.orderflow.order.dto.addresses.AddressRequest;
import com.orderflow.order.dto.addresses.AddressResponse;
import com.orderflow.order.dto.auth.UserProfile;
import com.orderflow.order.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/adresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getMyAddresses(@AuthenticationPrincipal UserProfile userProfile) {
        return ResponseEntity.ok(addressService.getAddressesByUser(userProfile.id()));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserProfile userProfile
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createAddress(request, userProfile.id()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request,
            @AuthenticationPrincipal UserProfile userProfile
    ) {
        return ResponseEntity.ok(addressService.updateAddress(id, request, userProfile.id()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id,
            @AuthenticationPrincipal UserProfile userProfile
    ) {
        addressService.deleteAddress(id, userProfile.id());
        return ResponseEntity.noContent().build();
    }
}
