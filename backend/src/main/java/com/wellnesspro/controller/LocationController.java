package com.wellnesspro.controller;

import com.wellnesspro.model.Location;
import com.wellnesspro.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationRepository locationRepository;

    @GetMapping
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }
}
