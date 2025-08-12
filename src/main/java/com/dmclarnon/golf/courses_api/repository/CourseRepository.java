package com.dmclarnon.golf.courses_api.repository;

import com.dmclarnon.golf.courses_api.model.Course;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface CourseRepository extends JpaRepository<Course, Long> {
    /*
    @Query("""
     SELECT c FROM Course c
     WHERE (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%'))
                     OR LOWER(c.location) LIKE LOWER(CONCAT('%', :q, '%')))
  """)
    Page<Course> search(String q, Pageable pageable);
    */


        Page<Course> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(
                String name, String location, Pageable pageable);

}
