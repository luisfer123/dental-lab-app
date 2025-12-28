package com.dentallab.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.WorkPriceEntity;

/**
 * WorkPriceRepository
 * -------------------------------------------------------------------------
 * Repository responsible for persisting and retrieving BASE PRICE snapshots
 * for works.
 *
 * SEMANTIC MEANING
 * ----------------
 * A WorkPriceEntity represents a DECISION:
 *
 *   "For this work, the base price has been fixed to this value."
 *
 * The relationship to WorkEntity is ONE-TO-ONE and UNIQUE.
 * There can be at most one base price per work.
 */
@Repository
public interface WorkPriceRepository
        extends JpaRepository<WorkPriceEntity, Long> {

    /**
     * Retrieves the fixed base price for a given work.
     *
     * @param workId the identifier of the work
     * @return the WorkPriceEntity if present, empty otherwise
     */
    Optional<WorkPriceEntity> findByWork_Id(Long workId);

    /**
     * Checks whether a base price has already been fixed for a work.
     *
     * @param workId the identifier of the work
     * @return true if a base price exists, false otherwise
     */
    boolean existsByWork_Id(Long workId);
}
