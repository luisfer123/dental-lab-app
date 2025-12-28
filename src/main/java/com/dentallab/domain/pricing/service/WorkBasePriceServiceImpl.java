package com.dentallab.domain.pricing.service;

import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkPriceEntity;
import com.dentallab.persistence.repository.WorkPriceRepository;

/**
 * <h2>WorkBasePriceServiceImpl</h2>
 *
 * <hr/>
 *
 * <p>
 * Service responsible for <strong>PREVIEWING</strong> and
 * <strong>FIXING</strong> the <em>base price</em> of a work.
 * </p>
 *
 * <p>
 * It is the public entry point for base price resolution.
 * {@link #previewBasePrice(PriceResolutionRequest)} uses
 * {@link WorkTypePriceResolver} to compute the standard base price
 * according to pricing rules.
 * </p>
 *
 * <p>
 * {@link #fixBasePrice(Long, BasePriceResult)} commits that price
 * as an explicit pricing decision.
 * </p>
 *
 * <p>
 * This service represents the <strong>boundary</strong> between:
 * </p>
 *
 * <ul>
 *   <li>pricing rules (dynamic, time-dependent, configurable)</li>
 *   <li>pricing decisions (explicit, stable, historically meaningful)</li>
 * </ul>
 *
 * <p>
 * In other words:
 * </p>
 *
 * <blockquote>
 *   Pricing rules can change.<br/>
 *   A fixed base price must <strong>not</strong> change implicitly.
 * </blockquote>
 *
 * <p>
 * This class exists to make that distinction explicit and enforceable.
 * </p>
 *
 * <hr/>
 *
 * <h3>Role in the pricing architecture</h3>
 *
 * <p>
 * The complete pricing lifecycle is intentionally split into stages:
 * </p>
 *
 * <ol>
 *   <li>
 *     <strong>Pricing rule evaluation</strong><br/>
 *     {@link WorkTypePriceResolver}<br/>
 *     <em>"According to rules, the base price would be X"</em>
 *   </li>
 *
 *   <li>
 *     <strong>Base price fixation (this service)</strong><br/>
 *     <em>"We now decide that the base price is X"</em><br/>
 *     Persisted in {@code work_price}
 *   </li>
 *
 *   <li>
 *     <strong>Final price resolution</strong><br/>
 *     {@link FinalWorkPriceResolver}<br/>
 *     <em>base price + overrides</em>
 *   </li>
 * </ol>
 *
 * <p>
 * This service implements <strong>stage (2)</strong>.
 * </p>
 *
 * <hr/>
 *
 * <h3>Why preview and fix are separate</h3>
 *
 * <h4>{@code previewBasePrice(...)</h4>
 * <ul>
 *   <li>evaluates pricing rules</li>
 *   <li>returns a hypothetical result</li>
 *   <li>has <strong>no</strong> side effects</li>
 *   <li>safe to call repeatedly</li>
 * </ul>
 *
 * <h4>{@code fixBasePrice(...)</h4>
 * <ul>
 *   <li>commits a pricing decision</li>
 *   <li>persists <strong>exactly</strong> what was previewed</li>
 *   <li><strong>must not</strong> recompute pricing rules</li>
 * </ul>
 *
 * <p>
 * This separation ensures:
 * </p>
 *
 * <ul>
 *   <li>temporal consistency (the user fixes what they actually saw)</li>
 *   <li>auditability (fixed prices are historical facts)</li>
 *   <li>protection against silent rule changes</li>
 * </ul>
 *
 * <hr/>
 *
 * <h3>Assumptions and responsibilities</h3>
 *
 * <ul>
 *   <li>
 *     This service assumes that all pricing inputs
 *     (including prosthetic unit counts) have been fully
 *     resolved <strong>upstream</strong>.
 *   </li>
 *   <li>
 *     It does <strong>not</strong> inspect clinical structure
 *     (e.g. crown vs bridge, teeth, pontics).
 *   </li>
 *   <li>
 *     It treats the incoming pricing result as authoritative
 *     for the purpose of fixation.
 *   </li>
 * </ul>
 *
 * <hr/>
 *
 * <h3>Hard requirements / invariants</h3>
 *
 * <ul>
 *   <li>a base price may only be <strong>fixed once</strong> per work</li>
 *   <li>fixing a base price is an explicit action</li>
 *   <li>fixing a base price <strong>must not</strong> recompute pricing rules</li>
 *   <li>
 *     a fixed base price becomes the authoritative source
 *     for all future pricing operations
 *   </li>
 * </ul>
 *
 * <p>
 * Violating these invariants is considered a workflow or programming error
 * and results in an {@link IllegalStateException}.
 * </p>
 *
 * <hr/>
 *
 * <h3>Important note for future maintainers</h3>
 *
 * <p>
 * If you ever feel tempted to:
 * </p>
 *
 * <ul>
 *   <li>recompute pricing rules inside {@code fixBasePrice(...)}</li>
 *   <li>merge preview and fix into a single method</li>
 *   <li>silently overwrite an existing base price</li>
 * </ul>
 *
 * <p><strong>Stop.</strong></p>
 *
 * <p>
 * Doing so would break the explicit pricing contract of the system
 * and reintroduce ambiguity that this service was designed to eliminate.
 * </p>
 */
@Service
public class WorkBasePriceServiceImpl implements WorkBasePriceService {

    private static final Logger log =
            LoggerFactory.getLogger(WorkBasePriceServiceImpl.class);

    /**
     * Resolver responsible for evaluating pricing rules
     * (work_type_price) and computing a hypothetical base price.
     *
     * This component has NO side effects.
     */
    private final WorkTypePriceResolver workTypePriceResolver;

    /**
     * Repository used to persist the fixed base price snapshot.
     *
     * A row in work_price represents an explicit pricing decision.
     */
    private final WorkPriceRepository workPriceRepository;

    /**
     * Used only to obtain a lightweight reference to WorkEntity
     * when persisting a WorkPriceEntity, without loading the work.
     */
    @PersistenceContext
    private EntityManager entityManager;

    public WorkBasePriceServiceImpl(
            WorkTypePriceResolver workTypePriceResolver,
            WorkPriceRepository workPriceRepository) {

        this.workTypePriceResolver =
                Objects.requireNonNull(workTypePriceResolver);
        this.workPriceRepository =
                Objects.requireNonNull(workPriceRepository);
    }

    /**
     * <p>
     * <strong>Preview</strong> the base price of a work according to
     * the current pricing rules.
     * </p>
     *
     * <p>
     * This method:
     * </p>
     *
     * <ul>
     *   <li>delegates pricing logic to {@link WorkTypePriceResolver}</li>
     *   <li>returns a {@link BasePriceResult} describing a hypothetical price</li>
     *   <li>does <strong>not</strong> persist anything</li>
     *   <li>does <strong>not</strong> modify system state</li>
     * </ul>
     *
     * <p>
     * It answers the question:
     * </p>
     *
     * <blockquote>
     *   If we were to fix the price right now, what would it be?
     * </blockquote>
     *
     * <h4>Typical use cases</h4>
     *
     * <ul>
     *   <li>UI preview before user confirmation</li>
     *   <li>validation flows</li>
     *   <li>administrative checks</li>
     * </ul>
     *
     * <h4>Assumptions</h4>
     *
     * <ul>
     *   <li>
     *     All pricing inputs (including prosthetic unit counts)
     *     are assumed to be fully resolved upstream.
     *   </li>
     *   <li>
     *     This method does <strong>not</strong> inspect clinical structure
     *     (e.g. crown vs bridge, teeth, pontics).
     *   </li>
     * </ul>
     *
     * @param request
     *        pricing context (workId, priceGroup, pricingDate)
     * @return
     *        {@link BasePriceResult} describing the computed base price
     */
    @Override
    @Transactional(readOnly = true)
    public BasePriceResult previewBasePrice(PriceResolutionRequest request) {

        log.debug(
                "Previewing base price for workId={}, priceGroup={}, pricingDate={}",
                request.getWorkId(),
                request.getPriceGroup(),
                request.getPricingDate()
        );

        BasePriceResult result =
                workTypePriceResolver.resolveBasePrice(request);

        log.info(
                "Base price previewed for workId={}: basePrice={}, currency={}, group={}",
                request.getWorkId(),
                result.getBasePrice(),
                result.getCurrency(),
                result.getPriceGroup()
        );

        return result;
    }

    /**
     * <p>
     * <strong>Fix</strong> the base price of a work using a previously
     * previewed result.
     * </p>
     *
     * <p>
     * This method represents an explicit <strong>commitment</strong>.
     * </p>
     *
     * <p>
     * It:
     * </p>
     *
     * <ul>
     *   <li>persists <strong>exactly</strong> the given {@link BasePriceResult}</li>
     *   <li>does <strong>not</strong> recompute pricing rules</li>
     *   <li>establishes the authoritative base price for the work</li>
     * </ul>
     *
     * <p>
     * This method answers the question:
     * </p>
     *
     * <blockquote>
     *   We have reviewed the proposed base price — commit it.
     * </blockquote>
     *
     * <h4>Important</h4>
     *
     * <p>
     * The {@link BasePriceResult} passed to this method is assumed to have been:
     * </p>
     *
     * <ul>
     *   <li>obtained via {@link #previewBasePrice(PriceResolutionRequest)}</li>
     *   <li>explicitly accepted by a user or automated workflow</li>
     * </ul>
     *
     * <p>
     * This method treats the provided {@link BasePriceResult} as
     * <strong>authoritative</strong> and must not:
     * </p>
     *
     * <ul>
     *   <li>re-evaluate pricing rules</li>
     *   <li>adjust prosthetic unit counts</li>
     *   <li>reinterpret pricing inputs</li>
     * </ul>
     *
     * @param workId
     *        the identifier of the work whose base price is being fixed
     * @param basePrice
     *        the previously previewed base price to be committed
     *
     * @throws IllegalStateException
     *         if a base price already exists for the given work
     */
    @Override
    @Transactional
    public void fixBasePrice(Long workId, BasePriceResult basePrice) {

        log.debug(
                "Fixing base price for workId={} using previewed value {} {}",
                workId,
                basePrice.getBasePrice(),
                basePrice.getCurrency()
        );

        // -----------------------------------------------------------------
        // Enforce invariant: base price may only be fixed once
        // -----------------------------------------------------------------
        if (workPriceRepository.existsByWork_Id(workId)) {
            log.error(
                    "Cannot fix base price: workId={} already has a base price",
                    workId
            );
            throw new IllegalStateException(
                    "Base price already fixed for work " + workId
            );
        }

        // -----------------------------------------------------------------
        // Attach work reference without loading the entity
        // -----------------------------------------------------------------
        WorkEntity workRef =
                entityManager.getReference(WorkEntity.class, workId);

        // -----------------------------------------------------------------
        // Persist EXACTLY the previewed base price
        // -----------------------------------------------------------------
        WorkPriceEntity entity = new WorkPriceEntity();
        entity.setWork(workRef);
        entity.setPrice(basePrice.getBasePrice());
        entity.setCurrency(basePrice.getCurrency());
        entity.setPriceGroup(basePrice.getPriceGroup());

        workPriceRepository.save(entity);

        log.info(
                "Base price FIXED for workId={}: basePrice={}, currency={}, group={}",
                workId,
                basePrice.getBasePrice(),
                basePrice.getCurrency(),
                basePrice.getPriceGroup()
        );
    }
}
