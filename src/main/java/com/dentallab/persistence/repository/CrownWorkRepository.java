package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.CrownWorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing CrownWorkEntity persistence operations.
 * One-to-one mapping with WorkEntity (work_id as shared primary key).
 */
@Repository
public interface CrownWorkRepository extends JpaRepository<CrownWorkEntity, Long> {
    
    // Optional: if you need quick lookup by base work ID (same as primary key)
    // CrownWorkEntity findByWork_Id(Long workId);
}
