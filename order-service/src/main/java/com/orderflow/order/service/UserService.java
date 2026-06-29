package com.orderflow.order.service;

import com.orderflow.order.model.User;
import com.orderflow.order.repository.UserRepository;
import com.orderflow.order.exceptions.ResourceAlreadyExistsException;
import com.orderflow.order.exceptions.ResourceNotFound;
import com.orderflow.order.dto.CreateUserRequest;
import com.orderflow.order.dto.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserProfile createUser(CreateUserRequest request) {
        String email = request.email().toLowerCase();

        log.info("Creating user [email={}]", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists [email={}]", email);
            throw new ResourceAlreadyExistsException("Email already in use");
        }

        String encryptedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(email)
                .firstName(request.firstName())
                .lastName(request.lastName())
                .password(encryptedPassword)
                .build();

        User savedUser = userRepository.save(user);

        log.info(
                "User created [userId={}, email={}]",
                savedUser.getId(),
                savedUser.getEmail()
        );

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found [email={}]", email);
                    return new ResourceNotFound("User with Email: '" + email + "' not found");
                });
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found [userId={}]", id);
                    return new ResourceNotFound("User with ID: '" + id + "' not found");
                });
    }

    private UserProfile mapToResponse(User user) {
        return new UserProfile(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}