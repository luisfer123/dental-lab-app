package com.dentallab.domain.pricing.persistence;

import com.dentallab.domain.pricing.model.WorkPricingView;
import com.dentallab.domain.pricing.query.*;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * <h2>WorkPricingQueryDispatcher</h2>
 *
 * <hr/>
 *
 * <p>
 * Dispatcher responsible for extracting <strong>PRICING PARAMETERS</strong>
 * from a work and delegating to the appropriate
 * family-specific pricing query.
 * </p>
 *
 * <h3>Important architectural role</h3>
 *
 * <p>
 * This class is the <strong>single entry point</strong> for retrieving a
 * {@link WorkPricingView} given a {@code workId}.
 * </p>
 *
 * <p>
 * It acts as a routing layer that:
 * </p>
 * <ul>
 *   <li>determines the work family</li>
 *   <li>selects the correct pricing query implementation</li>
 *   <li>hides work-family-specific logic from higher layers</li>
 * </ul>
 *
 * <p>
 * This ensures that:
 * </p>
 * <ul>
 *   <li>pricing resolvers do <strong>not</strong> need to know about work subtypes</li>
 *   <li>new work families can be added without changing resolver logic</li>
 * </ul>
 *
 * <h3>What this class does</h3>
 *
 * <ul>
 *   <li>receives a {@code workId}</li>
 *   <li>looks up the work family</li>
 *   <li>dispatches to a family-specific pricing query</li>
 *   <li>returns a normalized {@link WorkPricingView}</li>
 * </ul>
 *
 * <h3>What this class does <em>not</em> do</h3>
 *
 * <ul>
 *   <li>it does <strong>not</strong> resolve prices</li>
 *   <li>it does <strong>not</strong> apply pricing rules</li>
 *   <li>it does <strong>not</strong> apply overrides</li>
 *   <li>it does <strong>not</strong> persist anything</li>
 * </ul>
 *
 * <p>
 * This class answers the question:
 * </p>
 * <blockquote>
 *   Which pricing query should be used to <em>describe</em> this work for pricing?
 * </blockquote>
 *
 * <h3>Where it fits in the pricing pipeline</h3>
 *
 * <pre>
 *   workId
 *     ↓
 *   WorkPricingQueryDispatcher   (this class)
 *     ↓
 *   WorkPricingView              (pricing identity)
 *     ↓
 *   WorkTypePriceResolver        (pricing rules)
 * </pre>
 *
 * <h3>Hard requirements / invariants</h3>
 *
 * <ul>
 *   <li>The work <strong>must</strong> exist</li>
 *   <li>The work <strong>must</strong> belong to a supported pricing family</li>
 *   <li>A pricing query <strong>must</strong> exist for that family</li>
 * </ul>
 *
 * <p>
 * If a family is not supported, this class <strong>fails fast</strong>.
 * </p>
 */

@Repository
public class WorkPricingQueryDispatcher implements WorkPricingQuery {

    private static final Logger log =
            LoggerFactory.getLogger(WorkPricingQueryDispatcher.class);

    /**
     * Pricing query for FIXED_PROSTHESIS family.
     * Handles crowns, bridges, and other fixed restorations.
     */
    private final FixedProsthesisPricingQuery fixedQuery;

    /**
     * Lightweight lookup used to determine the work family
     * without loading the full work entity.
     */
    private final WorkFamilyLookupQuery familyLookup;

    public WorkPricingQueryDispatcher(
            FixedProsthesisPricingQuery fixedQuery,
            WorkFamilyLookupQuery familyLookup) {

        this.fixedQuery = fixedQuery;
        this.familyLookup = familyLookup;
    }

    /**
     * Retrieves a pricing identity (WorkPricingView) for a given work.
     *
     * Resolution steps:
     *  1. Determine the work family
     *  2. Dispatch to the family-specific pricing query
     *  3. Return a normalized pricing view
     *
     * This method MUST remain simple and deterministic.
     * All pricing-family branching logic is intentionally centralized here.
     *
     * @param workId the identifier of the work
     * @return a WorkPricingView describing pricing-relevant attributes
     * @throws IllegalStateException if pricing is not implemented
     *                               for the work family
     */
    @Override
    public WorkPricingView findByWorkId(Long workId) {

        log.debug("Dispatching pricing query for workId={}", workId);

        // -----------------------------------------------------------------
        // 1) Determine work family
        // -----------------------------------------------------------------
        WorkFamilyRefEntity family = familyLookup.findFamilyByWorkId(workId);

        if (family.getCode() == null) {
            log.error("Cannot resolve pricing parameters: work not found (id={})", workId);
            throw new IllegalStateException(
                    "Cannot resolve pricing parameters: work not found (id=" + workId + ")"
            );
        }

        log.debug("Work family resolved for workId={}: {}", workId, family.getCode());

        // -----------------------------------------------------------------
        // 2) Dispatch to family-specific pricing query
        // -----------------------------------------------------------------
        if ("FIXED_PROSTHESIS".equals(family.getCode())) {
            log.debug("Using FixedProsthesisPricingQuery for workId={}", workId);
            return fixedQuery.findFixedPricing(workId);
        }

        // -----------------------------------------------------------------
        // 3) Fail fast for unsupported families
        // -----------------------------------------------------------------
        log.error(
                "Pricing parameters extraction not implemented for work family={} (workId={})",
                family,
                workId
        );

        throw new IllegalStateException(
                "Pricing not implemented for family: " + family.getCode()
        );
    }
}
