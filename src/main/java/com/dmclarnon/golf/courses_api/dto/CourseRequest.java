package com.dmclarnon.golf.courses_api.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record CourseRequest(
        @NotBlank String name,
        @NotBlank String location,
        @NotNull @Min(9) @Max(18) Integer holes,
        @NotNull @Min(27) @Max(90) Integer parTotal,
        @NotNull @Size(min=9, max=18) List<@NotNull CourseHoleDto> holesDetail,
        @NotNull TeeSetRequest teeSet             // MVP: exactly one
) {}
