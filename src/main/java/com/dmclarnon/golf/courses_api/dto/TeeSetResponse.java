package com.dmclarnon.golf.courses_api.dto;

import java.math.BigDecimal;
import java.util.List;

public record TeeSetResponse(
        Long id,
        String name,
        String color,
        String gender,
        BigDecimal rating,
        Integer slope,
        Integer totalYards,
        List<TeeHoleDto> holes
) {}
