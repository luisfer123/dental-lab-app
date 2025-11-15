package com.dentallab.service.impl;

import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;
import com.dentallab.persistence.entity.WorkStatusRefEntity;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;
import com.dentallab.persistence.repository.WorkStatusRefRepository;
import com.dentallab.service.LookupService;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Central service that resolves all lookup table entities:
 *   - Work families (work_family_ref)
 *   - Work types (work_type_ref)
 *   - Work statuses (work_status_ref)
 *
 * Provides both:
 *   - Entity resolution (for JPA relations)
 *   - Human-readable label resolution (for API responses)
 *
 * Lookup results are cached for performance.
 */
@Service
public class LookupServiceImpl implements LookupService {

    private static final Logger log = LoggerFactory.getLogger(LookupServiceImpl.class);

    private final WorkFamilyRefRepository familyRepo;
    private final WorkTypeRefRepository typeRepo;
    private final WorkStatusRefRepository statusRepo;

    public LookupServiceImpl(
            WorkFamilyRefRepository familyRepo,
            WorkTypeRefRepository typeRepo,
            WorkStatusRefRepository statusRepo
    ) {
        this.familyRepo = familyRepo;
        this.typeRepo = typeRepo;
        this.statusRepo = statusRepo;
    }


    // ==========================================================
    // FAMILY LOOKUPS
    // ==========================================================

    @Override
    @Cacheable("familyByCode")
    public WorkFamilyRefEntity getFamilyEntity(String familyCode) {
        if (familyCode == null) {
            log.warn("getFamilyEntity called with NULL family code");
            return null;
        }

        log.debug("Resolving WorkFamilyRefEntity for code={}", familyCode);

        return familyRepo.findById(familyCode)
                .orElseThrow(() -> {
                    log.error("Invalid work family code={}", familyCode);
                    return new IllegalArgumentException("Invalid work family: " + familyCode);
                });
    }

    @Override
    @Cacheable("familyLabelByCode")
    public String getFamilyLabel(String familyCode) {
        if (familyCode == null) return null;

        return familyRepo.findById(familyCode)
                .map(WorkFamilyRefEntity::getLabel)
                .orElse(familyCode);
    }

    @Override
    public String getFamilyLabel(WorkFamilyRefEntity entity) {
        return entity != null ? entity.getLabel() : null;
    }

    @Override
    @Cacheable("allFamilies")
    public List<WorkFamilyRefEntity> getAllFamilies() {
        return familyRepo.findAll();
    }


    // ==========================================================
    // TYPE LOOKUPS
    // ==========================================================

    @Override
    @Cacheable("typeByCode")
    public WorkTypeRefEntity getTypeEntity(String typeCode) {
        if (typeCode == null) {
            log.warn("getTypeEntity called with NULL type code");
            return null;
        }

        log.debug("Resolving WorkTypeRefEntity for code={}", typeCode);

        return typeRepo.findById(typeCode)
                .orElseThrow(() -> {
                    log.error("Invalid work type code={}", typeCode);
                    return new IllegalArgumentException("Invalid work type: " + typeCode);
                });
    }

    @Override
    @Cacheable("typeLabelByCode")
    public String getTypeLabel(String typeCode) {
        if (typeCode == null) return null;

        return typeRepo.findById(typeCode)
                .map(WorkTypeRefEntity::getLabel)
                .orElse(typeCode);
    }

    @Override
    public String getTypeLabel(WorkTypeRefEntity entity) {
        return entity != null ? entity.getLabel() : null;
    }

    @Override
    @Cacheable("allTypes")
    public List<WorkTypeRefEntity> getAllTypes() {
        return typeRepo.findAll();
    }

    @Override
    @Cacheable(value = "typesByFamily", key = "#familyCode")
    public List<WorkTypeRefEntity> getTypesByFamily(String familyCode) {
        if (familyCode == null) return List.of();

        log.debug("Fetching work types for family={}", familyCode);

        return typeRepo.findByFamilyCode(familyCode);
    }


    // ==========================================================
    // STATUS LOOKUPS (NEW)
    // ==========================================================

    @Override
    @Cacheable("statusByCode")
    public WorkStatusRefEntity getStatusEntity(String statusCode) {
        if (statusCode == null) {
            log.warn("getStatusEntity called with NULL status code");
            return null;
        }

        log.debug("Resolving WorkStatusRefEntity for code={}", statusCode);

        return statusRepo.findById(statusCode)
                .orElseThrow(() -> {
                    log.error("Invalid work status code={}", statusCode);
                    return new IllegalArgumentException("Invalid work status: " + statusCode);
                });
    }

    @Override
    @Cacheable("statusLabelByCode")
    public String getStatusLabel(String statusCode) {
        if (statusCode == null) return null;

        return statusRepo.findById(statusCode)
                .map(WorkStatusRefEntity::getLabel)
                .orElse(statusCode);
    }

    @Override
    public String getStatusLabel(WorkStatusRefEntity entity) {
        return entity != null ? entity.getLabel() : null;
    }

    @Override
    @Cacheable("allStatuses")
    public List<WorkStatusRefEntity> getAllStatuses() {
        return statusRepo.findAll();
    }
}
