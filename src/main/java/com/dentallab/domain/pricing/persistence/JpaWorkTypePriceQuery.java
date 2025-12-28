package com.dentallab.domain.pricing.persistence;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dentallab.domain.pricing.query.WorkTypePriceQuery;
import com.dentallab.persistence.entity.WorkTypePriceEntity;

/**
 * JpaWorkTypePriceQuery
 * -------------------------------------------------------------------------
 * JPA repository implementation responsible for locating the MOST
 * APPLICABLE pricing rule from the `work_type_price` table.
 *
 * IMPORTANT: THIS QUERY DEFINES PRICING POLICY
 * --------------------------------------------
 * This repository does not merely fetch data — it ENFORCES how pricing
 * rules are selected when multiple candidates exist.
 *
 * In other words:
 *   The ORDER BY clause here IS BUSINESS LOGIC.
 *
 * ROLE IN THE PRICING ARCHITECTURE
 * --------------------------------
 * This query is used exclusively by:
 *   - WorkTypePriceResolver
 *
 * It answers the question:
 *   "Given a pricing identity and a date, which pricing rule SHOULD apply?"
 *
 * It does NOT:
 *   - compute prices
 *   - apply overrides
 *   - persist or modify pricing rules
 *
 * SELECTION RULES (EXPLICIT CONTRACT)
 * -----------------------------------
 * A pricing rule is eligible if:
 *   1) work_family matches exactly
 *   2) work_type matches exactly
 *   3) price_group matches exactly
 *   4) valid_from <= pricingDate
 *   5) For each optional attribute:
 *        - rule value IS NULL (wildcard), OR
 *        - rule value equals the requested value
 *
 * If multiple rules are eligible:
 *   - Prefer the MOST SPECIFIC rule
 *     (i.e., the one with more non-null attributes)
 *   - If specificity ties, prefer the MOST RECENT rule
 *     (largest valid_from)
 *
 * The query guarantees:
 *   - At most ONE rule is returned
 *   - Deterministic selection
 *
 * DATABASE ASSUMPTIONS
 * --------------------
 * - This query is written for MySQL / MariaDB
 * - Boolean expressions in ORDER BY evaluate to 1 (true) or 0 (false)
 * - LIMIT 1 is used to enforce single-result semantics
 *
 * CHANGING THIS QUERY
 * -------------------
 * Any change to:
 *   - WHERE conditions
 *   - ORDER BY clauses
 *
 * MUST be considered a change in PRICING POLICY and reviewed accordingly.
 */
@Repository
public interface JpaWorkTypePriceQuery
        extends org.springframework.data.repository.Repository<WorkTypePriceEntity, Long>,
                WorkTypePriceQuery {

    /**
     * Finds the best matching pricing rule for the given pricing identity.
     *
     * Matching logic summary:
     *  - Exact match on family, type, and price group
     *  - Optional attributes act as wildcards when NULL
     *  - Rules must be effective on or before the given pricing date
     *  - More specific rules override generic ones
     *  - Newer rules override older ones when specificity ties
     *
     * @param workFamily        work family code (e.g. FIXED_PROSTHESIS)
     * @param workType          work type code (e.g. CROWN, BRIDGE)
     * @param priceGroup        pricing group (e.g. DEFAULT, UNIVERSITY)
     * @param constitution      optional attribute (NULL means any)
     * @param buildingTechnique optional attribute (NULL means any)
     * @param coreMaterialId    optional attribute (NULL means any)
     * @param pricingDate       date at which pricing must be valid
     *
     * @return the most applicable WorkTypePriceEntity, or empty if none exists
     *
     * @implNote
     * This method relies on native SQL to precisely control:
     *  - wildcard behavior
     *  - specificity ordering
     *  - temporal validity
     *
     * JPQL is intentionally not used here.
     */
    @Override
    @Query(value = """
        SELECT *
        FROM work_type_price wtp
        WHERE wtp.work_family = :workFamily
          AND wtp.work_type   = :workType
          AND wtp.price_group = :priceGroup
          AND wtp.valid_from <= :pricingDate

          -- Optional attributes:
          -- NULL in rule means "applies to any value"
          AND (wtp.constitution IS NULL OR wtp.constitution = :constitution)
          AND (wtp.building_technique IS NULL OR wtp.building_technique = :buildingTechnique)
          AND (wtp.core_material_id IS NULL OR wtp.core_material_id = :coreMaterialId)

        -- Specificity ordering:
        -- Rules with more defined attributes are preferred
        ORDER BY
          (wtp.constitution IS NOT NULL) DESC,
          (wtp.building_technique IS NOT NULL) DESC,
          (wtp.core_material_id IS NOT NULL) DESC,

          -- Temporal ordering:
          -- Newer rules override older ones
          wtp.valid_from DESC

        LIMIT 1
        """, nativeQuery = true)
    Optional<WorkTypePriceEntity> findBestMatch(
            @Param("workFamily") String workFamily,
            @Param("workType") String workType,
            @Param("priceGroup") String priceGroup,
            @Param("constitution") String constitution,
            @Param("buildingTechnique") String buildingTechnique,
            @Param("coreMaterialId") Long coreMaterialId,
            @Param("pricingDate") LocalDate pricingDate
    );
}
