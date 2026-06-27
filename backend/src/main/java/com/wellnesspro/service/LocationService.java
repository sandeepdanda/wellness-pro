package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.LocationResponse;
import com.wellnesspro.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<LocationResponse> getAll() {
        return locationRepository.findAll().stream().map(mapper::toLocation).toList();
    }
}
