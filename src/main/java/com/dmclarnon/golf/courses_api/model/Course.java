package com.dmclarnon.golf.courses_api.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "course")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Course {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=120)
    private String name;

    @Column(nullable=false, length=160)
    private String location;

    @Column(nullable=false)
    private Short holes;               // 9 or 18

    @Column(name="par_total", nullable=false)
    private Short parTotal;

    @Column(name="owner_username", nullable=false, length=100)
    private String ownerUsername;

    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    @OrderBy("number ASC")
    private List<Hole> holeList = new ArrayList<>();

    @OneToMany(mappedBy="course", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<TeeSet> teeSets = new ArrayList<>();
}
