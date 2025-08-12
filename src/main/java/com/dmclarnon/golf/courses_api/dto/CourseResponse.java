package com.dmclarnon.golf.courses_api.dto;

import java.util.List;

public record CourseResponse(
        Long id,
        String name,
        String location,
        int holes,
        int parTotal,
        String ownerUsername,
        List<CourseHoleDto> holesDetail,
        List<TeeSetResponse> teeSets
) {}
