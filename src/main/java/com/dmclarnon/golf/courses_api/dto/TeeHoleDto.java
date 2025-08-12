package com.dmclarnon.golf.courses_api.dto;

import jakarta.validation.constraints.*;

public record TeeHoleDto(
        @NotNull @Min(1) @Max(18) Integer number,
        @NotNull @Min(10) @Max(1000) Integer yards
) {}
