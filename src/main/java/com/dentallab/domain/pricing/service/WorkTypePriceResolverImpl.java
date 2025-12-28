package com.dentallab.domain.pricing.service;

import java.math.BigDecimal;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.model.WorkPricingView;
import com.dentallab.domain.pricing.query.WorkPricingQuery;
import com.dentallab.domain.pricing.query.WorkTypePriceQuery;
import com.dentallab.persistence.entity.WorkTypePriceEntity;

/**
 * <h2>WorkTypePriceResolverImpl</h2>
 *
 * <hr/>
 *
 * <p>
 * This class is responsible for resolving the <strong>base price</strong>
 * of a work according to pricing rules stored in the
 * {@code work_type_price} table.
 * </p>
 *
 * <h3>Important architectural role</h3>
 *
 * <p>This resolver:</p>
 *
 * <ul>
 *   <li><strong>does</strong> decide which pricing rule applies</li>
 *   <li><strong>does</strong> compute the base price (before overrides)</li>
 * </ul>
 *
 * <p>This resolver <strong>does not</strong>:</p>
 *
 * <ul>
 *   <li>apply price overrides</li>
 *   <li>consult or modify {@code work_price}</li>
 *   <li>produce a final price</li>
 * </ul>
 *
 * <p>
 * It answers the question:
 * </p>
 *
 * <blockquote>
 *   According to current pricing rules, how much <em>should</em> this work cost?
 * </blockquote>
 *
 * <h3>Pricing flow context</h3>
 *
 * <p>
 * The pricing architecture is intentionally split into two stages:
 * </p>
 *
 * <ol>
 *   <li>
 *     <strong>Base price resolution</strong> (this class)
 *     <ul>
 *       <li>uses {@code work_type_price}</li>
 *       <li>depends on work characteristics</li>
 *       <li>may change over time as pricing rules evolve</li>
 *     </ul>
 *   </li>
 *
 *   <li>
 *     <strong>Final price resolution</strong>
 *     ({@link FinalWorkPriceResolver})
 *     <ul>
 *       <li>uses {@code work_price} plus overrides</li>
 *       <li>must remain stable once the base price is fixed</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>
 * This class implements <strong>stage (1)</strong>.
 * </p>
 *
 * <h3>Why {@code WorkPricingView} is used</h3>
 *
 * <p>
 * Works are polymorphic (e.g. crown, bridge).
 * Pricing rules require a normalized view of
 * pricing-relevant attributes.
 * </p>
 *
 * <p>
 * {@link WorkPricingView} provides that normalized
 * <em>pricing identity</em> by:
 * </p>
 *
 * <ul>
 *   <li>extracting attributes from the appropriate extension table</li>
 *   <li>unifying them into a single projection</li>
 * </ul>
 *
 * <h3>Assumptions</h3>
 *
 * <ul>
 *   <li>
 *     Prosthetic unit counts (including bridge aggregation)
 *     are assumed to be fully resolved upstream and provided
 *     via {@link WorkPricingView}.
 *   </li>
 *   <li>
 *     This resolver does <strong>not</strong> compute or infer
 *     prosthetic units.
 *   </li>
 * </ul>
 *
 * <h3>Hard requirements / invariants</h3>
 *
 * <ul>
 *   <li>the work <strong>must</strong> exist</li>
 *   <li>
 *     at least one applicable {@code work_type_price} rule
 *     <strong>must</strong> exist
 *   </li>
 *   <li>
 *     a rule <strong>must</strong> define either:
 *     <ul>
 *       <li>{@code base_price}</li>
 *       <li>
 *         {@code price_per_unit}
 *         (with valid prosthetic units)
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p>
 * Violating any of these invariants results in an
 * {@link IllegalStateException}.
 * </p>
 */
@Service
public class WorkTypePriceResolverImpl implements WorkTypePriceResolver {

    private static final Logger log =
            LoggerFactory.getLogger(WorkTypePriceResolverImpl.class);

    /**
     * Dispatcher used to extract pricing parameters from a work.
     * This component hides work-family-specific queries (crown, bridge, etc.).
     */
    private final WorkPricingQuery workPricingQuery;

    /**
     * Query used to locate the best matching pricing rule
     * from the `work_type_price` table.
     */
    private final WorkTypePriceQuery workTypePriceQuery;

    public WorkTypePriceResolverImpl(
            WorkPricingQuery workPricingQuery,
            WorkTypePriceQuery workTypePriceQuery) {

        this.workPricingQuery = Objects.requireNonNull(workPricingQuery);
        this.workTypePriceQuery = Objects.requireNonNull(workTypePriceQuery);
    }

    /**
     * <p>
     * Resolves the <strong>BASE price</strong> for a work according to pricing rules.
     * </p>
     *
     * <p><strong>Resolution steps:</strong></p>
     * <ol>
     *   <li>Load pricing-relevant attributes of the work (<code>WorkPricingView</code>)</li>
     *   <li>Locate the best matching rule in <code>work_type_price</code></li>
     *   <li>
     *     Compute the base price:
     *     <ul>
     *       <li><code>base_price</code> if defined</li>
     *       <li>
     *         OR <code>price_per_unit</code> × <code>prostheticUnits</code>
     *       </li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>This method:</strong></p>
     * <ul>
     *   <li><strong>DOES NOT</strong> persist anything</li>
     *   <li><strong>DOES NOT</strong> apply overrides</li>
     *   <li><strong>DOES NOT</strong> require <code>work_price</code> to exist</li>
     * </ul>
     *
     * @param request pricing context (<code>workId</code>, <code>priceGroup</code>, <code>pricingDate</code>)
     * @return {@link BasePriceResult} containing the computed base price and metadata
     * @throws IllegalStateException
     *         if the work or a pricing rule is missing,
     *         or if the pricing rule is invalid
     */
    @Override
    public BasePriceResult resolveBasePrice(PriceResolutionRequest request) {

        Long workId = request.getWorkId();
        log.debug("Resolving BASE price for workId={}", workId);

        // -----------------------------------------------------------------
        // 1) Load pricing parameters for the work
        // -----------------------------------------------------------------
        WorkPricingView view = workPricingQuery.findByWorkId(workId);

        if (view == null) {
            log.error("Cannot resolve base price: work not found (id={})", workId);
            throw new IllegalStateException(
                    "Cannot resolve base price: work not found (id=" + workId + ")"
            );
        }

        log.debug(
                "Pricing parameters loaded for workId={}: family={}, type={}, constitution={}, technique={}, coreMaterialId={}, units={}",
                workId,
                view.getWorkFamily(),
                view.getWorkType(),
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                view.getProstheticUnits()
        );

        // -----------------------------------------------------------------
        // 2) Locate best matching pricing rule
        // -----------------------------------------------------------------
        WorkTypePriceEntity rule = workTypePriceQuery.findBestMatch(
                view.getWorkFamily(),
                view.getWorkType(),
                request.getPriceGroup(),
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                request.getPricingDate()
        ).orElseThrow(() -> {
            log.error(
                    "No work_type_price rule found for workId={}, family={}, type={}, group={}, date={}",
                    workId,
                    view.getWorkFamily(),
                    view.getWorkType(),
                    request.getPriceGroup(),
                    request.getPricingDate()
            );
            return new IllegalStateException(
                    "No work_type_price rule found for workId=" + workId
                            + " (family=" + view.getWorkFamily()
                            + ", type=" + view.getWorkType()
                            + ", group=" + request.getPriceGroup()
                            + ", date=" + request.getPricingDate()
                            + ")"
            );
        });

        log.debug(
                "Pricing rule selected: priceId={}, basePrice={}, pricePerUnit={}, currency={}, group={}",
                rule.getPriceId(),
                rule.getBasePrice(),
                rule.getPricePerUnit(),
                rule.getCurrency(),
                rule.getPriceGroup()
        );

        // -----------------------------------------------------------------
        // 3) Compute base price from rule
        // -----------------------------------------------------------------
        BigDecimal base = computeBase(rule, view);

        log.info(
                "Base price resolved for workId={}: basePrice={}, currency={}, priceGroup={}",
                workId,
                base,
                rule.getCurrency(),
                rule.getPriceGroup()
        );

        // -----------------------------------------------------------------
        // 4) Return base price result
        // -----------------------------------------------------------------
        return new BasePriceResult(
                base,
                rule.getCurrency(),
                rule.getPriceGroup(),
                rule.getPriceId()
        );
    }

    /**
     * <p>
     * Computes the <strong>base price</strong> from a pricing rule.
     * </p>
     *
     * <p><strong>Computation rules:</strong></p>
     * <ul>
     *   <li>
     *     If <code>base_price</code> is defined &rarr; use it directly
     *   </li>
     *   <li>
     *     Else if <code>price_per_unit</code> is defined &rarr;
     *     multiply by <code>prostheticUnits</code>
     *   </li>
     *   <li>
     *     Otherwise &rarr; the rule is considered <strong>invalid</strong>
     *   </li>
     * </ul>
     *
     * @param rule the pricing rule selected from <code>work_type_price</code>
     * @param view the pricing parameters of the work
     * @return the computed base price
     * @throws IllegalStateException
     *         if the pricing rule definition is invalid
     */
    private BigDecimal computeBase(WorkTypePriceEntity rule,
                                   WorkPricingView view) {

        if (rule.getBasePrice() != null) {
            return rule.getBasePrice();
        }

        if (rule.getPricePerUnit() != null) {
            Integer units = view.getProstheticUnits();

            if (units == null || units <= 0) {
                log.error(
                        "Invalid pricing rule: price_per_unit requires prostheticUnits, but got {} for workId={}",
                        units,
                        view.getWorkId()
                );
                throw new IllegalStateException(
                        "price_per_unit rule requires prostheticUnits, but got "
                                + units + " for workId=" + view.getWorkId()
                );
            }

            return rule.getPricePerUnit()
                    .multiply(BigDecimal.valueOf(units));
        }

        log.error(
                "Invalid pricing rule: both base_price and price_per_unit are null (priceId={})",
                rule.getPriceId()
        );

        throw new IllegalStateException(
                "Invalid work_type_price rule: both base_price and price_per_unit are null (priceId="
                        + rule.getPriceId() + ")"
        );
    }
}
