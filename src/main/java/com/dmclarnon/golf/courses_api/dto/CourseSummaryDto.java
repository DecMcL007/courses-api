package com.dmclarnon.golf.courses_api.dto;

public record CourseSummaryDto(
        Long id, String name, String location, int holes, int parTotal, String ownerUsername
) {}
