package com.dentallab.domain.pricing.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;

/**
 * <p>
 * Public service responsible for providing the <strong>current final price</strong>
 * of a work.
 * </p>
 *
 * <p>
 * This is the public entry point for final price resolution.
 * {@link FinalWorkPriceResolver} is used internally to perform
 * the actual computation.
 * </p>
 *
 * <h3>Important architectural role</h3>
 *
 * <p>
 * This service is an <strong>orchestrator</strong>.
 * </p>
 *
 * <p>
 * It does <strong>not</strong>:
 * </p>
 *
 * <ul>
 *   <li>resolve base prices</li>
 *   <li>consult {@code work_type_price}</li>
 *   <li>apply pricing rules</li>
 *   <li>persist pricing data</li>
 * </ul>
 *
 * <p>
 * Its sole responsibility is to:
 * </p>
 *
 * <ul>
 *   <li>expose a stable entry point for pricing</li>
 *   <li>delegate final price computation to {@link FinalWorkPriceResolver}</li>
 *   <li>enforce pricing invariants at the service boundary</li>
 * </ul>
 *
 * <h3>Pricing pipeline context</h3>
 *
 * <p>
 * The complete pricing pipeline is intentionally split into stages:
 * </p>
 *
 * <ol>
 *   <li>
 *     <strong>Base price resolution (rules-based)</strong>
 *     <ul>
 *       <li>uses {@code work_type_price}</li>
 *       <li>occurs at work creation or price-fixing time</li>
 *     </ul>
 *   </li>
 *
 *   <li>
 *     <strong>Base price persistence</strong>
 *     <ul>
 *       <li>stored in {@code work_price}</li>
 *       <li>represents a frozen snapshot</li>
 *     </ul>
 *   </li>
 *
 *   <li>
 *     <strong>Final price resolution (this service)</strong>
 *     <ul>
 *       <li>uses {@code work_price} plus overrides</li>
 *       <li>can be called repeatedly and safely</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <p>
 * This service implements <strong>stage (3)</strong>.
 * </p>
 *
 * <h3>Hard requirements / invariants</h3>
 *
 * <ul>
 *   <li>a work <strong>must</strong> already have a base price stored in {@code work_price}</li>
 *   <li>overrides (if any) must be associated with that base price</li>
 * </ul>
 *
 * <p>
 * Violating these invariants is considered a programming or workflow error
 * and will result in an exception being thrown by the resolver.
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class WorkPricingServiceImpl implements WorkPricingService {

    private static final Logger log =
            LoggerFactory.getLogger(WorkPricingServiceImpl.class);

    private final FinalWorkPriceResolver finalPriceResolver;

    public WorkPricingServiceImpl(FinalWorkPriceResolver finalPriceResolver) {
        this.finalPriceResolver = finalPriceResolver;
    }

    /**
     * <p>
     * Returns the <strong>current final price</strong> of a work.
     * </p>
     *
     * <p>
     * This method:
     * </p>
     *
     * <ul>
     *   <li>delegates directly to {@link FinalWorkPriceResolver}</li>
     *   <li>recomputes the final price <strong>every time</strong> it is called</li>
     *   <li>reflects any overrides added or removed since the last call</li>
     * </ul>
     *
     * <p>
     * The returned price represents the authoritative result of:
     * </p>
     *
     * <ul>
     *   <li>the fixed base price stored in {@code work_price}</li>
     *   <li>plus any currently active overrides</li>
     * </ul>
     *
     * <h4>Safe usage scenarios</h4>
     *
     * <ul>
     *   <li>UI display</li>
     *   <li>invoice previews</li>
     *   <li>auditing</li>
     *   <li>repricing after overrides are added or removed</li>
     * </ul>
     *
     * <p>
     * This method is intentionally <strong>side-effect free</strong> and may be
     * called repeatedly without altering system state.
     * </p>
     *
     * @param request
     *        pricing context containing the {@code workId}
     * @return
     *        {@link PriceResolution} containing the base price, applied overrides,
     *        and the resulting final price
     *
     * @throws IllegalStateException
     *         if pricing invariants are violated (e.g. missing base price)
     */
    @Override
    public PriceResolution resolveFinalPrice(PriceResolutionRequest request) {

        log.debug(
                "Resolving final price through WorkPricingService for workId={}",
                request.getWorkId()
        );

        PriceResolution resolution =
                finalPriceResolver.resolve(request);

        log.info(
                "Final price delivered for workId={}: finalPrice={}, currency={}",
                request.getWorkId(),
                resolution.getFinalPrice(),
                resolution.getCurrency()
        );

        return resolution;
    }
}
