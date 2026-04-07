package com.wellnesspro.repository;

import com.wellnesspro.model.FitnessClass;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FitnessClassRepository extends JpaRepository<FitnessClass, Long> {
    List<FitnessClass> findByLocationId(Long locationId);
}
