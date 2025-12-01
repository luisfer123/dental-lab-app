package com.dentallab.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentallab.persistence.entity.WorkStatusRefEntity;

public interface WorkStatusRefRepository extends JpaRepository<WorkStatusRefEntity, String> {
	
	Optional<WorkStatusRefEntity> findByCode(String code);

    List<WorkStatusRefEntity> findAllByOrderBySequenceOrderAsc();

    Optional<WorkStatusRefEntity> findBySequenceOrder(Integer order);

    boolean existsBySequenceOrder(Integer order);
}
