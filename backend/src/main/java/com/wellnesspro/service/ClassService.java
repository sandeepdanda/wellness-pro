package com.wellnesspro.service;

import com.wellnesspro.dto.Dtos.ClassResponse;
import com.wellnesspro.dto.Dtos.CreateClassRequest;
import com.wellnesspro.exception.ApiExceptions.BadRequestException;
import com.wellnesspro.exception.ApiExceptions.NotFoundException;
import com.wellnesspro.model.FitnessClass;
import com.wellnesspro.model.Location;
import com.wellnesspro.repository.FitnessClassRepository;
import com.wellnesspro.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassService {

    private final FitnessClassRepository classRepository;
    private final LocationRepository locationRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<ClassResponse> getAll() {
        return classRepository.findAll().stream().map(mapper::toClass).toList();
    }

    @Transactional(readOnly = true)
    public List<ClassResponse> getByLocation(Long locationId) {
        return classRepository.findByLocationId(locationId).stream().map(mapper::toClass).toList();
    }

    @Transactional(readOnly = true)
    public ClassResponse getById(Long id) {
        return mapper.toClass(findOrThrow(id));
    }

    @Transactional
    public ClassResponse create(CreateClassRequest request) {
        if (request.maxCapacity() <= 0) {
            throw new BadRequestException("maxCapacity must be greater than zero");
        }
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new NotFoundException("Location " + request.locationId() + " not found"));
        FitnessClass fitnessClass = FitnessClass.builder()
                .name(request.name())
                .instructor(request.instructor())
                .location(location)
                .schedule(request.schedule())
                .maxCapacity(request.maxCapacity())
                .currentEnrollment(0)
                .build();
        return mapper.toClass(classRepository.save(fitnessClass));
    }

    @Transactional
    public ClassResponse update(Long id, CreateClassRequest request) {
        if (request.maxCapacity() <= 0) {
            throw new BadRequestException("maxCapacity must be greater than zero");
        }
        FitnessClass fitnessClass = findOrThrow(id);
        if (request.maxCapacity() < fitnessClass.getCurrentEnrollment()) {
            throw new BadRequestException("maxCapacity cannot be below current enrollment of "
                    + fitnessClass.getCurrentEnrollment());
        }
        Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new NotFoundException("Location " + request.locationId() + " not found"));
        fitnessClass.setName(request.name());
        fitnessClass.setInstructor(request.instructor());
        fitnessClass.setLocation(location);
        fitnessClass.setSchedule(request.schedule());
        fitnessClass.setMaxCapacity(request.maxCapacity());
        return mapper.toClass(classRepository.save(fitnessClass));
    }

    @Transactional
    public void delete(Long id) {
        FitnessClass fitnessClass = findOrThrow(id);
        classRepository.delete(fitnessClass);
    }

    private FitnessClass findOrThrow(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class " + id + " not found"));
    }
}
