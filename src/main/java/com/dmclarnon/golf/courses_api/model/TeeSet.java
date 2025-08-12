package com.dmclarnon.golf.courses_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "tee_set",
        uniqueConstraints=@UniqueConstraint(name="uq_tee_name_per_course", columnNames={"course_id","name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeeSet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="course_id")
    private Course course;

    @Column(nullable=false, length=50)
    private String name;           // "White", "Yellow", "Default", etc.

    private String color;          // optional ("white" / "#ffffff")
    private String gender;         // optional ("Men", "Women")

    private BigDecimal rating;     // optional
    private Short slope;           // optional
    @Column(name="total_yards")
    private Integer totalYards;    // optional; can compute from teeHoles

    @OneToMany(mappedBy="teeSet", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<TeeHole> teeHoles = new ArrayList<>();
}
