package com.dentallab.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.TechnicianProfileEntity;

@Repository
public interface TechnicianProfileRepository extends JpaRepository<TechnicianProfileEntity, Long> {
	Optional<TechnicianProfileEntity> findByClient_Id(Long clientId);
}
