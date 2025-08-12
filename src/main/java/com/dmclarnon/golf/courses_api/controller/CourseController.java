package com.dmclarnon.golf.courses_api.controller;

import com.dmclarnon.golf.courses_api.dto.*;
import com.dmclarnon.golf.courses_api.service.CourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService service;

    @GetMapping
    public Page<CourseSummaryDto> list(
            @RequestParam(required=false) String q,
            @PageableDefault(size=20, sort="name") Pageable pageable) {
        return service.list(q, pageable);
    }

    @GetMapping("/{id}")
    public CourseResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_courses.write')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CourseRequest req, Authentication auth) {
        return service.create(req, auth);
    }

    @PreAuthorize("hasAuthority('SCOPE_courses.write') and @ownership.canModifyCourse(#id, authentication)")
    @PutMapping("/{id}")
    public CourseResponse update(@PathVariable Long id, @Valid @RequestBody CourseRequest req, Authentication auth) {
        return service.update(id, req, auth);
    }

    @PreAuthorize("hasAuthority('SCOPE_courses.write') and @ownership.canModifyCourse(#id, authentication)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, auth);
    }
}
