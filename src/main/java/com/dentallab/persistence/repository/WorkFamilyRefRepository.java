package com.dentallab.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dentallab.persistence.entity.WorkFamilyRefEntity;

public interface WorkFamilyRefRepository extends JpaRepository<WorkFamilyRefEntity, String> {
	
	Optional<WorkFamilyRefEntity> findByCode(String code);
}
