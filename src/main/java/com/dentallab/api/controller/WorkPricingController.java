package com.dentallab.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkBasePriceService;
import com.dentallab.domain.pricing.service.WorkPricingService;

/**
 * WorkPricingController
 * -------------------------------------------------------------------------
 * REST controller exposing pricing operations for works.
 *
 * IMPORTANT:
 * This controller exposes USER INTENT, not pricing internals.
 *
 * It does NOT:
 *  - call pricing resolvers directly
 *  - recompute prices implicitly
 *  - bypass pricing invariants
 *
 * All pricing logic is delegated to domain services.
 */
@RestController
@RequestMapping("/api/works/{workId}/pricing")
public class WorkPricingController {

    private final WorkBasePriceService basePriceService;
    private final WorkPricingService pricingService;

    public WorkPricingController(
            WorkBasePriceService basePriceService,
            WorkPricingService pricingService) {

        this.basePriceService = basePriceService;
        this.pricingService = pricingService;
    }

    /**
     * PREVIEW base price according to pricing rules.
     *
     * This endpoint:
     *  - evaluates pricing rules
     *  - does NOT persist anything
     *  - is safe to call repeatedly
     *
     * Typical use:
     *  - UI shows the price before confirmation
     */
    @PostMapping("/preview")
    public ResponseEntity<BasePriceResult> previewBasePrice(
            @PathVariable Long workId,
            @RequestBody PriceResolutionRequest request) {

        BasePriceResult result =
                basePriceService.previewBasePrice(
                        request.withWorkId(workId)
                );

        return ResponseEntity.ok(result);
    }

    /**
     * FIX base price using a previously previewed result.
     *
     * This endpoint represents an explicit COMMITMENT.
     * The price sent here is assumed to have been reviewed and accepted.
     */
    @PostMapping("/fix")
    public ResponseEntity<Void> fixBasePrice(
            @PathVariable Long workId,
            @RequestBody BasePriceResult previewedPrice) {

        basePriceService.fixBasePrice(workId, previewedPrice);

        return ResponseEntity.ok().build();
    }

    /**
     * GET the current FINAL price of the work.
     *
     * This endpoint:
     *  - uses stored base price
     *  - applies all overrides
     *  - reflects the current final amount
     *
     * Safe for:
     *  - UI display
     *  - invoices
     *  - previews
     */
    @GetMapping("/final")
    public ResponseEntity<PriceResolution> getFinalPrice(
            @PathVariable Long workId) {

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        PriceResolution resolution =
                pricingService.resolveFinalPrice(request);

        return ResponseEntity.ok(resolution);
    }
}
