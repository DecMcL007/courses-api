package com.dmclarnon.golf.courses_api.dto;

// TeeSetRequest.java (one teeset in create/update for now)

import jakarta.validation.constraints.*;
import java.util.List;

public record TeeSetRequest(
        @NotBlank String name,      // e.g. "White" or "Default"
        String color,
        String gender,
        @Size(min=1, max=18) List<@NotNull TeeHoleDto> holes
) {}
