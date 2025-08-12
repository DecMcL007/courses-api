package com.dmclarnon.golf.courses_api.security;

import com.dmclarnon.golf.courses_api.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ownership")
@RequiredArgsConstructor
public class OwnershipChecker {
    private final CourseRepository repo;

    public boolean canModifyCourse(Long id, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return repo.findById(id)
                .map(c -> c.getOwnerUsername().equalsIgnoreCase(auth.getName()))
                .orElse(false);
    }

}
