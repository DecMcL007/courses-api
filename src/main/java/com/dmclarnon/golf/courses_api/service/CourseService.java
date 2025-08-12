package com.dmclarnon.golf.courses_api.service;

import com.dmclarnon.golf.courses_api.dto.*;
import com.dmclarnon.golf.courses_api.model.*;
import com.dmclarnon.golf.courses_api.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepo;
/*
    public Page<CourseSummaryDto> list(String q, Pageable pageable) {
        return courseRepo.search(q, pageable)
                .map(c -> new CourseSummaryDto(
                        c.getId(), c.getName(), c.getLocation(),
                        c.getHoles(), c.getParTotal(), c.getOwnerUsername()
                ));
    } */

    public Page<CourseSummaryDto> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return courseRepo.findAll(pageable)
                    .map(c -> new CourseSummaryDto(
                            c.getId(), c.getName(), c.getLocation(),
                            c.getHoles(), c.getParTotal(), c.getOwnerUsername()
                    ));
        }
        return courseRepo
                .findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(q, q, pageable)
                .map(c -> new CourseSummaryDto(
                        c.getId(), c.getName(), c.getLocation(),
                        c.getHoles(), c.getParTotal(), c.getOwnerUsername()
                ));
    }

    public CourseResponse get(Long id) {
        Course c = courseRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
        return toResponse(c);
    }

    @Transactional
    public CourseResponse create(CourseRequest req, Authentication auth) {
        validateCourse(req);

        String owner = auth != null ? auth.getName() : "anonymous";

        Course c = Course.builder()
                .name(req.name())
                .location(req.location())
                .holes(req.holes().shortValue())
                .parTotal(req.parTotal().shortValue())
                .ownerUsername(owner)
                .build();

        // Holes (par + SI)
        var holes = new ArrayList<Hole>(req.holesDetail().size());
        for (var h : req.holesDetail()) {
            holes.add(Hole.builder()
                    .course(c)
                    .number(h.number().shortValue())
                    .par(h.par().shortValue())
                    .strokeIndex(h.strokeIndex().shortValue())
                    .build());
        }
        c.setHoleList(holes);

        // One tee set (MVP)
        var tsReq = req.teeSet();
        TeeSet ts = TeeSet.builder()
                .course(c)
                .name(tsReq.name())
                .color(tsReq.color())
                .gender(tsReq.gender())
                .build();

        // Map yards to holes by hole number
        Map<Integer, Hole> holesByNum = new HashMap<>();
        for (Hole h : holes) holesByNum.put(h.getNumber().intValue(), h);

        var teeHoles = new ArrayList<TeeHole>(tsReq.holes().size());
        int sumYards = 0;
        for (var th : tsReq.holes()) {
            Hole hole = holesByNum.get(th.number());
            if (hole == null) throw new IllegalArgumentException("Tee hole references unknown hole number: " + th.number());
            teeHoles.add(TeeHole.builder()
                    .teeSet(ts)
                    .hole(hole)
                    .yards(th.yards())
                    .build());
            sumYards += th.yards();
        }
        ts.setTotalYards(sumYards);
        ts.setTeeHoles(teeHoles);

        c.setTeeSets(List.of(ts));

        Course saved = courseRepo.save(c);
        return toResponse(saved);
    }

    @Transactional
    public CourseResponse update(Long id, CourseRequest req, Authentication auth) {
        validateCourse(req);

        Course c = courseRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
        enforceOwner(c, auth);

        c.setName(req.name());
        c.setLocation(req.location());
        c.setHoles(req.holes().shortValue());
        c.setParTotal(req.parTotal().shortValue());

        // Replace holes
        c.getHoleList().clear();
        for (var h : req.holesDetail()) {
            c.getHoleList().add(Hole.builder()
                    .course(c)
                    .number(h.number().shortValue())
                    .par(h.par().shortValue())
                    .strokeIndex(h.strokeIndex().shortValue())
                    .build());
        }

        // Replace tee sets with one teeset (MVP)
        c.getTeeSets().clear();

        var tsReq = req.teeSet();
        TeeSet ts = TeeSet.builder()
                .course(c)
                .name(tsReq.name())
                .color(tsReq.color())
                .gender(tsReq.gender())
                .build();

        Map<Integer, Hole> holesByNum = new HashMap<>();
        for (Hole h : c.getHoleList()) holesByNum.put(h.getNumber().intValue(), h);

        var teeHoles = new ArrayList<TeeHole>(tsReq.holes().size());
        int sumYards = 0;
        for (var th : tsReq.holes()) {
            Hole hole = holesByNum.get(th.number());
            if (hole == null) throw new IllegalArgumentException("Tee hole references unknown hole number: " + th.number());
            teeHoles.add(TeeHole.builder()
                    .teeSet(ts)
                    .hole(hole)
                    .yards(th.yards())
                    .build());
            sumYards += th.yards();
        }
        ts.setTotalYards(sumYards);
        ts.setTeeHoles(teeHoles);

        c.getTeeSets().add(ts);

        return toResponse(c); // dirty checking persists
    }

    @Transactional
    public void delete(Long id, Authentication auth) {
        Course c = courseRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Course not found"));
        enforceOwner(c, auth);
        courseRepo.delete(c);
    }

    // ---------- helpers ----------

    private void enforceOwner(Course c, Authentication auth) {
        if (auth == null || !Objects.equals(c.getOwnerUsername(), auth.getName())) {
            throw new AccessDeniedException("Not owner");
        }
    }

    private void validateCourse(CourseRequest req) {
        // holes count
        if (!Objects.equals(req.holes(), req.holesDetail().size())) {
            throw new IllegalArgumentException("holes must equal holesDetail size");
        }
        // unique number & SI; sum(par) == parTotal
        Set<Integer> nums = new HashSet<>();
        Set<Integer> sis  = new HashSet<>();
        int sumPar = 0;
        for (var h : req.holesDetail()) {
            if (!nums.add(h.number())) throw new IllegalArgumentException("Duplicate hole number: " + h.number());
            if (!sis.add(h.strokeIndex())) throw new IllegalArgumentException("Duplicate stroke index: " + h.strokeIndex());
            sumPar += h.par();
        }
        if (sumPar != req.parTotal()) {
            throw new IllegalArgumentException("Sum of pars must equal parTotal");
        }

        // teeset holes must align with holesDetail numbers and be unique
        Set<Integer> teeNums = new HashSet<>();
        for (var th : req.teeSet().holes()) {
            if (!teeNums.add(th.number())) throw new IllegalArgumentException("Duplicate tee hole number: " + th.number());
            if (!nums.contains(th.number())) throw new IllegalArgumentException("Tee hole number not in holesDetail: " + th.number());
        }
        if (teeNums.size() != req.holes()) {
            throw new IllegalArgumentException("teeSet holes must cover all course holes");
        }
    }

    private CourseResponse toResponse(Course c) {
        var holes = c.getHoleList().stream()
                .sorted(Comparator.comparingInt(h -> h.getNumber().intValue()))
                .map(h -> new CourseHoleDto(h.getNumber().intValue(), h.getPar().intValue(), h.getStrokeIndex().intValue()))
                .toList();

        var tees = c.getTeeSets().stream()
                .map(ts -> new TeeSetResponse(
                        ts.getId(),
                        ts.getName(),
                        ts.getColor(),
                        ts.getGender(),
                        ts.getRating(),
                        ts.getSlope() == null ? null : ts.getSlope().intValue(),
                        ts.getTotalYards(),
                        ts.getTeeHoles().stream()
                                .sorted(Comparator.comparingInt(th -> th.getHole().getNumber().intValue()))
                                .map(th -> new TeeHoleDto(th.getHole().getNumber().intValue(), th.getYards()))
                                .toList()
                ))
                .toList();

        return new CourseResponse(
                c.getId(), c.getName(), c.getLocation(),
                c.getHoles(), c.getParTotal(), c.getOwnerUsername(),
                holes, tees
        );
    }
}
