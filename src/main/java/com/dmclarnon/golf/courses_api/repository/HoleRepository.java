package com.dmclarnon.golf.courses_api.repository;

import com.dmclarnon.golf.courses_api.model.Hole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoleRepository extends JpaRepository<Hole, Long> {}
