package com.wellnesspro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fitness_classes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FitnessClass {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;          // e.g., "Yoga", "HIIT", "Spin"
    private String instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private LocalDateTime schedule;
    private int maxCapacity;
    private int currentEnrollment;
}
