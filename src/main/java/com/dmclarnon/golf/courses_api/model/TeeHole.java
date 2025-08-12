package com.dmclarnon.golf.courses_api.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="tee_hole",
        uniqueConstraints=@UniqueConstraint(name="uq_tee_hole", columnNames={"tee_set_id","hole_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TeeHole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="tee_set_id")
    private TeeSet teeSet;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="hole_id")
    private Hole hole;

    @Column(nullable=false)
    private Integer yards;     // 10..1000
}
