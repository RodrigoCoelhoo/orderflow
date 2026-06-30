package com.orderflow.order.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "First name cannot be blank")
    @Size(max = 30)
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+$", message = "Only letters are allowed")
    String firstName,

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 30)
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "Only letters are allowed")
    String lastName,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must have between 8 and 100 characters.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&\\-_])[A-Za-z\\d@$!%*?&\\-_]+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character (@$!%*?&-_)"
    )
    String password
) {}