package com.wellnesspro.controller;

import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.repository.FitnessClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final FitnessClassRepository classRepository;

    @GetMapping
    public List<FitnessClass> getAllClasses() {
        return classRepository.findAll();
    }

    @GetMapping("/location/{locationId}")
    public List<FitnessClass> getClassesByLocation(@PathVariable Long locationId) {
        return classRepository.findByLocationId(locationId);
    }

    // TODO: Add create, update, delete (admin only)
}
