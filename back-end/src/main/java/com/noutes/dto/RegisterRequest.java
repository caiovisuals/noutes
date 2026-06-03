package com.noutes.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @Size(min = 8) @NotBlank String password
) {}