package com.dentallab.persistence.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.persistence.entity.WorkTypePriceEntity;

/**
 * WorkTypePriceRepository
 * ---------------------------------------------------------------------
 * Write-side repository for WorkTypePriceEntity.
 *
 * RESPONSIBILITIES
 * ----------------
 * - Persist pricing rules
 * - Simple administrative retrieval
 *
 * NON-RESPONSIBILITIES
 * --------------------
 * - Pricing rule selection logic
 * - Specificity resolution
 * - Business ordering rules
 *
 * Those concerns are handled exclusively by:
 *   {@link com.dentallab.domain.pricing.persistence.JpaWorkTypePriceQuery}
 *
 * This repository is intentionally boring.
 */
@Repository
public interface WorkTypePriceRepository
        extends JpaRepository<WorkTypePriceEntity, Long> {

    /**
     * Finds all pricing rules for a given family + type.
     * Useful for admin screens, audits, or tests.
     */
    List<WorkTypePriceEntity> findByWorkFamilyAndWorkType(
            String workFamily,
            String workType
    );

    /**
     * Finds all pricing rules effective from a given date onward.
     * Useful for validations and diagnostics.
     */
    List<WorkTypePriceEntity> findByValidFromLessThanEqual(
            LocalDate date
    );
    
    @Query("""
            select
                case when count(w) > 0 then true else false end
            from WorkTypePriceEntity w
            where w.workFamily = :family
              and w.workType = :type
              and w.priceGroup = :priceGroup
              and coalesce(w.constitution, '*') = coalesce(:constitution, '*')
              and coalesce(w.buildingTechnique, '*') = coalesce(:buildingTechnique, '*')
              and coalesce(w.coreMaterialId, 0) = coalesce(:coreMaterialId, 0)
              and coalesce(w.dentureVariant, '*') = coalesce(:dentureVariant, '*')
              and coalesce(w.resinType, '*') = coalesce(:resinType, '*')
              and coalesce(w.teethType, '*') = coalesce(:teethType, '*')
              and w.validFrom = :validFrom
        """)
        boolean existsExactRule(
            @Param("family") String family,
            @Param("type") String type,
            @Param("priceGroup") String priceGroup,
            @Param("constitution") String constitution,
            @Param("buildingTechnique") String buildingTechnique,
            @Param("coreMaterialId") Long coreMaterialId,
            @Param("dentureVariant") String dentureVariant,
            @Param("resinType") String resinType,
            @Param("teethType") String teethType,
            @Param("validFrom") LocalDate validFrom
        );

}
