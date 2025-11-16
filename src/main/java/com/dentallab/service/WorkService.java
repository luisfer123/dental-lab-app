package com.dentallab.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.model.FullWorkModel;
import com.dentallab.api.model.WorkModel;

/**
 * Service layer interface for managing dental works (e.g., crowns, bridges, inlays).
 * Provides CRUD, pagination, and dynamic filtering capabilities.
 */
@Transactional(readOnly = true)
public interface WorkService {

    // ==========================================================
    // BASIC CRUD
    // ==========================================================

    /**
     * Returns a paginated list of works (optionally filtered by type).
     */
    List<WorkModel> getAll(int page, int size, String type);

    /**
     * Returns a single work by its unique ID.
     */
    WorkModel getById(Long id);

    /**
     * Persists a new work.
     */
    @Transactional
    WorkModel create(WorkModel model);

    /**
     * Updates an existing work with the given ID.
     */
    @Transactional
    WorkModel update(Long id, WorkModel model);

    /**
     * Deletes a work by ID.
     */
    @Transactional
    void delete(Long id);

    // ==========================================================
    // FULL (AGGREGATED) WORK VIEW
    // ==========================================================

    /**
     * Returns a full representation of a work including
     * its extension tables (e.g., crown_work, bridge_work)
     * and related entities such as steps, materials, etc.
     */
    FullWorkModel getFullById(Long id);

    // ==========================================================
    // DYNAMIC FILTERED PAGINATION
    // ==========================================================

    /**
     * Returns a paginated, dynamically filtered list of works.
     * Supports filtering by:
     * - work type (from lookup table)
     * - work family (from lookup table)
     * - status
     * - client ID
     * - one or more categories (many-to-many dynamic tags)
     *
     * @param page        Page number (0-based)
     * @param size        Page size
     * @param sortParams  Sort array, e.g. ["createdAt,desc"]
     * @param type        Work type code (optional)
     * @param status      Work status (optional)
     * @param clientId    Associated client ID (optional)
     * @param family      Work family code (optional)
     * @param categories  List of category names to match (optional)
     * @return Paginated page of filtered WorkModel
     */
    Page<WorkModel> getFiltered(
            int page,
            int size,
            String[] sortParams,
            String type,
            String status,
            Long clientId,
            String family,
            List<String> categories
    );
    
    Page<WorkModel> getFilteredWithClients(
            int page,
            int size,
            String[] sortParams,
            String typeCode,
            String statusCode,
            Long clientId,
            String familyCode,
            List<String> categories
    );
}
