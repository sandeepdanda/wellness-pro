package com.wellnesspro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "locations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Location {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;
    private int capacity;
    private String operatingHours;  // e.g., "6:00 AM - 10:00 PM"
}
