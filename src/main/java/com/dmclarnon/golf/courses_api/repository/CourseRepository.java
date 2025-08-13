package com.dmclarnon.golf.courses_api.repository;

import com.dmclarnon.golf.courses_api.model.Course;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface CourseRepository extends JpaRepository<Course, Long> {
        Page<Course> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(
                String name, String location, Pageable pageable);

}
