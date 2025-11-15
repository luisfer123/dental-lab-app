package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.DentistProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DentistProfileRepository extends JpaRepository<DentistProfileEntity, Long> {
    Optional<DentistProfileEntity> findByClient_Id(Long clientId);
}
