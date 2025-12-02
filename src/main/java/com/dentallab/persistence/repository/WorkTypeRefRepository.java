package com.dentallab.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentallab.persistence.entity.WorkTypeRefEntity;

public interface WorkTypeRefRepository extends JpaRepository<WorkTypeRefEntity, String> {

    List<WorkTypeRefEntity> findByFamilyCode(String familyCode);
    
    Optional<WorkTypeRefEntity> findByCode(String code);
}
