package com.dmclarnon.golf.courses_api.dto;

import jakarta.validation.constraints.*;

public record CourseHoleDto(
        @NotNull @Min(1) @Max(18) Integer number,
        @NotNull @Min(3) @Max(6) Integer par,
        @NotNull @Min(1) @Max(18) Integer strokeIndex
) {}
