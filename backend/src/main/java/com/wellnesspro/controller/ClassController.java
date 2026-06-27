package com.wellnesspro.controller;

import com.wellnesspro.dto.Dtos.ClassResponse;
import com.wellnesspro.dto.Dtos.CreateClassRequest;
import com.wellnesspro.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping
    public List<ClassResponse> getAllClasses() {
        return classService.getAll();
    }

    @GetMapping("/{id}")
    public ClassResponse getClass(@PathVariable Long id) {
        return classService.getById(id);
    }

    @GetMapping("/location/{locationId}")
    public List<ClassResponse> getClassesByLocation(@PathVariable Long locationId) {
        return classService.getByLocation(locationId);
    }

    // Mutations are restricted to ADMIN by SecurityConfig.
    @PostMapping
    public ResponseEntity<ClassResponse> createClass(@Valid @RequestBody CreateClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classService.create(request));
    }

    @PutMapping("/{id}")
    public ClassResponse updateClass(@PathVariable Long id, @Valid @RequestBody CreateClassRequest request) {
        return classService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClass(@PathVariable Long id) {
        classService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
