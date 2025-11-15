package com.dentallab.service;

import java.util.List;

import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;
import com.dentallab.persistence.entity.WorkStatusRefEntity;

/**
 * LookupService centralizes access to all lookup tables used by the workflow:
 *
 *   • work_family_ref
 *   • work_type_ref
 *   • work_status_ref
 *
 * It provides:
 *   - Entity resolution (String code → LookupEntity)
 *   - Label resolution (String code → human-readable label)
 *   - List retrieval for UI dropdowns and validation
 *
 * This abstraction prevents duplication and keeps lookup logic consistent,
 * especially when mapping between WorkEntity ↔ WorkModel and extensions.
 */
public interface LookupService {

    // ==========================================================
    // FAMILY LOOKUPS
    // ==========================================================

    /**
     * Resolves a family code to the corresponding lookup entity.
     *
     * @param familyCode the code from work_family_ref
     * @return the matching WorkFamilyRefEntity
     * @throws IllegalArgumentException if the code does not exist
     */
    WorkFamilyRefEntity getFamilyEntity(String familyCode);

    /**
     * Returns the label for a family code (or the code itself if not found).
     */
    String getFamilyLabel(String familyCode);

    /**
     * Returns the human-readable label for an already resolved family entity.
     */
    String getFamilyLabel(WorkFamilyRefEntity entity);

    /**
     * @return list of all defined work families.
     */
    List<WorkFamilyRefEntity> getAllFamilies();


    // ==========================================================
    // TYPE LOOKUPS
    // ==========================================================

    /**
     * Resolves a type code to the corresponding lookup entity.
     *
     * @param typeCode the code from work_type_ref
     * @return the matching WorkTypeRefEntity
     * @throws IllegalArgumentException if invalid
     */
    WorkTypeRefEntity getTypeEntity(String typeCode);

    /**
     * Returns the label for a type code.
     */
    String getTypeLabel(String typeCode);

    /**
     * Returns the label for a resolved type entity.
     */
    String getTypeLabel(WorkTypeRefEntity entity);

    /**
     * @return list of all defined work types.
     */
    List<WorkTypeRefEntity> getAllTypes();

    /**
     * Returns all types grouped under a specific work family.
     *
     * @param familyCode work family (e.g., FIXED_PROSTHESIS)
     */
    List<WorkTypeRefEntity> getTypesByFamily(String familyCode);


    // ==========================================================
    // STATUS LOOKUPS  (NEW)
    // ==========================================================

    /**
     * Resolves a work status code to its lookup entity.
     *
     * @param statusCode code from work_status_ref (e.g. IN_PROGRESS)
     * @return WorkStatusRefEntity or error if code does not exist
     */
    WorkStatusRefEntity getStatusEntity(String statusCode);

    /**
     * Returns the human-readable label for a status code.
     */
    String getStatusLabel(String statusCode);

    /**
     * Returns the label for a resolved status entity.
     */
    String getStatusLabel(WorkStatusRefEntity entity);

    /**
     * @return list of all defined statuses (for dropdowns, filters).
     */
    List<WorkStatusRefEntity> getAllStatuses();
}
