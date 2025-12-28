package com.dentallab.domain.pricing.service;

import com.dentallab.domain.pricing.model.*;
import com.dentallab.domain.pricing.query.*;
import com.dentallab.persistence.entity.WorkItemPriceOverrideEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * <strong>FinalWorkPriceResolverImpl</strong>
 * <hr>
 *
 * <p>
 * This class is responsible for resolving the <strong>FINAL price</strong> of a work.
 * </p>
 *
 * <p><strong>IMPORTANT ARCHITECTURAL ROLE</strong></p>
 *
 * <p>
 * This resolver <strong>DOES NOT</strong> decide the base price of a work.
 * </p>
 *
 * <p>It assumes that:</p>
 * <ul>
 *   <li>A base price has already been resolved and persisted in <code>work_price</code></li>
 *   <li>That base price is the authoritative starting point</li>
 * </ul>
 *
 * <p>Its <strong>ONLY</strong> responsibility is to:</p>
 * <ul>
 *   <li>Load the stored base price for a work</li>
 *   <li>Load all price overrides associated with that base price</li>
 *   <li>Aggregate overrides</li>
 *   <li>Produce a final price with full traceability</li>
 * </ul>
 *
 * <p>In other words:</p>
 * <ul>
 *   <li><em>Base price resolution</em> &rarr; handled elsewhere (<code>work_type_price</code> logic)</li>
 *   <li><em>Base price persistence</em> &rarr; handled elsewhere (price fixing / creation)</li>
 *   <li><em>Final price computation</em> &rarr; handled <strong>HERE</strong></li>
 * </ul>
 *
 * <p><strong>WHY THIS SEPARATION EXISTS</strong></p>
 *
 * <p>
 * Pricing rules (<code>work_type_price</code>) may change over time.
 * A fixed base price for a specific work <strong>MUST NOT</strong> change implicitly.
 * </p>
 *
 * <p>Therefore:</p>
 * <ul>
 *   <li><code>work_price</code> is the source of truth for the base price of a work</li>
 *   <li>Overrides are the <strong>ONLY</strong> dynamic component applied on top of that base</li>
 * </ul>
 *
 * <p>This class may be safely called:</p>
 * <ul>
 *   <li>Multiple times</li>
 *   <li>After new overrides are added</li>
 *   <li>For previews, invoices, or UI display</li>
 * </ul>
 *
 * <p><strong>HARD REQUIREMENT / INVARIANT</strong></p>
 *
 * <p>
 * A work <strong>MUST</strong> have a corresponding row in <code>work_price</code>
 * before this resolver can be used.
 * </p>
 *
 * <p>
 * If that invariant is violated, this resolver will throw an exception.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class FinalWorkPriceResolverImpl implements FinalWorkPriceResolver {

    private static final Logger log =
            LoggerFactory.getLogger(FinalWorkPriceResolverImpl.class);

    private final WorkPricingQuery pricingQuery;
    private final WorkPriceQuery workPriceQuery;
    private final WorkPriceOverrideQuery overrideQuery;

    public FinalWorkPriceResolverImpl(
            WorkPricingQuery pricingQuery,
            WorkPriceQuery workPriceQuery,
            WorkPriceOverrideQuery overrideQuery) {

        this.pricingQuery = pricingQuery;
        this.workPriceQuery = workPriceQuery;
        this.overrideQuery = overrideQuery;
    }

    /**
     * <p>
     * Resolves the <strong>final price</strong> of a work.
     * </p>
     *
     * <p><strong>Resolution steps:</strong></p>
     * <ol>
     *   <li>Validate that the work exists</li>
     *   <li>Load the persisted base price from <code>work_price</code></li>
     *   <li>Load all overrides associated with that base price</li>
     *   <li>Aggregate override adjustments</li>
     *   <li>Compute final price = base price + overrides</li>
     * </ol>
     *
     * <p><strong>This method NEVER:</strong></p>
     * <ul>
     *   <li>Resolves pricing rules</li>
     *   <li>Consults <code>work_type_price</code></li>
     *   <li>Persists or mutates pricing data</li>
     * </ul>
     *
     * @param request contains the <code>workId</code> whose price is being resolved
     * @return a complete {@link PriceResolution} with base price, overrides and final price
     * @throws IllegalStateException if the work does not exist or has no base price
     */
    @Override
    public PriceResolution resolve(PriceResolutionRequest request) {

        Long workId = request.getWorkId();
        log.debug("Resolving final price for workId={}", workId);

        // -----------------------------------------------------------------
        // 1) Validate work existence
        // -----------------------------------------------------------------
        WorkPricingView work = pricingQuery.findByWorkId(workId);

        if (work == null) {
            log.error("Cannot resolve price: work not found (id={})", workId);
            throw new IllegalStateException(
                    "Cannot resolve price: work not found (id=" + workId + ")"
            );
        }

        // -----------------------------------------------------------------
        // 2) Load base price (MUST exist)
        // -----------------------------------------------------------------
        var workPrice = workPriceQuery.findByWorkId(workId)
                .orElseThrow(() -> {
                    log.error(
                            "Cannot resolve final price: no base price defined for workId={}",
                            workId
                    );
                    return new IllegalStateException(
                            "No base price defined for work " + workId
                    );
                });

        log.debug(
                "Base price loaded for workId={}, priceId={}, basePrice={}, currency={}, priceGroup={}",
                workId,
                workPrice.getPriceId(),
                workPrice.getPrice(),
                workPrice.getCurrency(),
                workPrice.getPriceGroup()
        );

        // -----------------------------------------------------------------
        // 3) Load overrides associated with this base price
        // -----------------------------------------------------------------
        var overrides =
                overrideQuery.findByWorkPriceId(workPrice.getPriceId());

        log.debug(
                "Found {} price override(s) for workId={}, priceId={}",
                overrides.size(),
                workId,
                workPrice.getPriceId()
        );

        // -----------------------------------------------------------------
        // 4) Aggregate override adjustments
        // -----------------------------------------------------------------
        BigDecimal totalOverrides = overrides.stream()
                .map(WorkItemPriceOverrideEntity::getAdjustment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalPrice =
                workPrice.getPrice().add(totalOverrides);

        log.info(
                "Final price resolved for workId={}: base={}, overrides={}, final={}",
                workId,
                workPrice.getPrice(),
                totalOverrides,
                finalPrice
        );

        // -----------------------------------------------------------------
        // 5) Build override info for traceability / UI / audit
        // -----------------------------------------------------------------
        List<PriceOverrideInfo> overrideInfos = overrides.stream()
                .map(o -> new PriceOverrideInfo(
                        o.getAdjustment(),
                        o.getReason(),
                        o.getCreatedAt(),
                        o.getCreatedBy()
                ))
                .toList();

        // -----------------------------------------------------------------
        // 6) Return final resolution
        // -----------------------------------------------------------------
        return new PriceResolution(
                workPrice.getPrice(),
                totalOverrides,
                finalPrice,
                workPrice.getCurrency(),
                workPrice.getPriceId(),
                workPrice.getPriceGroup(),
                overrideInfos
        );
    }
}
