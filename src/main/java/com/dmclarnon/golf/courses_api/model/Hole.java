package com.dmclarnon.golf.courses_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="hole",
        uniqueConstraints = {
                @UniqueConstraint(name="uq_hole_number", columnNames={"course_id","number"}),
                @UniqueConstraint(name="uq_hole_si", columnNames={"course_id","stroke_index"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Hole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="course_id")
    private Course course;

    @Column(nullable=false)
    private Short number;        // 1..18

    @Column(nullable=false)
    private Short par;           // 3..6

    @Column(name="stroke_index", nullable=false)
    private Short strokeIndex;   // 1..18
}
