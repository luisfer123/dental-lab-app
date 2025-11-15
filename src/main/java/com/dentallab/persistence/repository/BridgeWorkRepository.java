package com.dentallab.persistence.repository;

import com.dentallab.persistence.entity.BridgeWorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing BridgeWorkEntity persistence operations.
 * Each BridgeWorkEntity shares its ID with its parent WorkEntity.
 */
@Repository
public interface BridgeWorkRepository extends JpaRepository<BridgeWorkEntity, Long> {
    
    // Optional helper for consistency or lazy-loading behavior
    // BridgeWorkEntity findByWork_Id(Long workId);
}
