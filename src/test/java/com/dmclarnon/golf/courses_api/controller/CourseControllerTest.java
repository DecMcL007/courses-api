package com.dmclarnon.golf.courses_api.controller;

import com.dmclarnon.golf.courses_api.dto.CourseResponse;
import com.dmclarnon.golf.courses_api.dto.CourseSummaryDto;
import com.dmclarnon.golf.courses_api.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for CourseController.
 * Note: Provide a bean named "ownership" to satisfy SpEL in @PreAuthorize.
 */
@WebMvcTest(CourseController.class)
@Import(CourseControllerTest.OwnershipStubConfig.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    private CourseResponse sampleCourse;

    @TestConfiguration
    static class OwnershipStubConfig {
        /** Bean name must be "ownership" to match SpEL: @ownership.canModifyCourse(#id, authentication) */
        @Bean(name = "ownership")
        public OwnershipGuard ownershipGuard() {
            return new OwnershipGuard();
        }

        /** Minimal stub with the expected method signature used by SpEL. */
        static class OwnershipGuard {
            public boolean canModifyCourse(Long id, Authentication authentication) {
                return true;
            }
        }
    }

    @BeforeEach
    void setUp() {
        sampleCourse = new CourseResponse(
                1L,
                "Augusta National",
                "GA, USA",
                18,
                72,
                "declan",
                List.of(),        // holesDetail
                List.of()         // teeSets
        );
    }

    @Test
    void list_returnsPagedCourses() throws Exception {
        Page<CourseSummaryDto> page = new PageImpl<>(
                List.of(
                        new CourseSummaryDto(
                                1L,
                                "Augusta National",
                                "GA, USA",
                                18,
                                72,
                                "declan"
                        )
                ),
                PageRequest.of(0, 20),
                1
        );

        given(courseService.list(ArgumentMatchers.isNull(), ArgumentMatchers.any())).willReturn(page);

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Augusta National"))
                .andExpect(jsonPath("$.content[0].location").value("GA, USA"))
                .andExpect(jsonPath("$.content[0].holes").value(18))
                .andExpect(jsonPath("$.content[0].parTotal").value(72))
                .andExpect(jsonPath("$.content[0].ownerUsername").value("declan"));

        verify(courseService).list(ArgumentMatchers.isNull(), ArgumentMatchers.any());
    }

    @Test
    void get_returnsCourseById() throws Exception {
        given(courseService.get(1L)).willReturn(sampleCourse);

        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Augusta National"))
                .andExpect(jsonPath("$.parTotal").value(72))
                .andExpect(jsonPath("$.ownerUsername").value("declan"));
        verify(courseService).get(1L);
    }

    @Test
    void create_requiresScope_whenAnonymous_forbidden() throws Exception {
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Augusta National","location":"GA, USA","holes":18,"parTotal":72}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_courses.write")
    void create_returnsCreatedCourse() throws Exception {
        given(courseService.create(ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(sampleCourse);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Augusta National","location":"GA, USA","holes":18,"parTotal":72}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Augusta National"));
        verify(courseService).create(ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_courses.write")
    void update_returnsUpdatedCourse() throws Exception {
        given(courseService.update(eq(1L), ArgumentMatchers.any(), ArgumentMatchers.any())).willReturn(sampleCourse);

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Augusta National","location":"GA, USA","holes":18,"parTotal":72}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Augusta National"));
        verify(courseService).update(eq(1L), ArgumentMatchers.any(), ArgumentMatchers.any());
    }

    @Test
    void update_requiresScope_whenAnonymous_forbidden() throws Exception {
        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Augusta National","location":"GA, USA","holes":18,"parTotal":72}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_courses.write")
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/courses/1"))
                .andExpect(status().isNoContent());
        verify(courseService).delete(eq(1L), ArgumentMatchers.any());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_courses.write")
    void create_validationError_returnsBadRequest() throws Exception {
        // Missing "name" (assuming @NotBlank on name in CourseRequest)
        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"location":"GA, USA","holes":18,"parTotal":72}
                                """))
                .andExpect(status().isBadRequest());
    }
}