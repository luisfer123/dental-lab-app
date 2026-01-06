package com.dentallab.api.controller;

import com.dentallab.domain.payment.dto.PaymentPreviewRequest;
import com.dentallab.domain.payment.dto.PaymentPreviewResult;
import com.dentallab.domain.payment.dto.RegisterPaymentRequest;
import com.dentallab.domain.payment.service.PaymentPreviewService;
import com.dentallab.domain.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * PaymentController
 * -----------------------------------------------------------------------------
 * API entry point for the 2-phase payment workflow:
 *
 * <h3>1) Preview (read-only)</h3>
 * Computes a non-authoritative allocation plan for a payment amount across selected works.
 * The preview can produce warnings and may require explicit balance confirmation.
 *
 * <h3>2) Register (authoritative commit)</h3>
 * Persists a real payment and its allocations exactly as confirmed by the technician.
 * This phase re-validates invariants at commit time and is idempotent via {@code idempotencyKey}.
 *
 * <p>
 * IMPORTANT: The controller must not compute allocations or pricing. That logic lives strictly
 * in domain services.
 * </p>
 */
@RestController
@RequestMapping("/api/payments")
@Validated
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentPreviewService paymentPreviewService;
    private final PaymentService paymentService;

    public PaymentController(
            PaymentPreviewService paymentPreviewService,
            PaymentService paymentService
    ) {
        this.paymentPreviewService = paymentPreviewService;
        this.paymentService = paymentService;
    }

    /**
     * Computes a read-only preview of how a payment would be allocated across selected works.
     *
     * <p>
     * This endpoint does not persist anything. It may return:
     * <ul>
     *   <li>computed per-work allocations</li>
     *   <li>unallocated remainder</li>
     *   <li>warnings</li>
     *   <li>suggested additional works</li>
     *   <li>requiresBalanceConfirmation flag</li>
     * </ul>
     * </p>
     *
     * @param request preview input (client, payment amount, selected works, optional overrides)
     * @return PaymentPreviewResult containing explicit allocation outcome
     */
    @PostMapping("/preview")
    public ResponseEntity<PaymentPreviewResult> preview(@Valid @RequestBody PaymentPreviewRequest request) {

        Objects.requireNonNull(request, "request must not be null");

        log.info(
                "Payment preview requested: clientId={}, amount={}, selectedWorks={}",
                request.getClientId(),
                request.getPaymentAmount(),
                request.getSelectedWorkIds() != null ? request.getSelectedWorkIds().size() : 0
        );

        PaymentPreviewResult result = paymentPreviewService.preview(request);

        log.info(
                "Payment preview completed: clientId={}, amount={}, totalAllocated={}, remainingUnallocated={}, requiresBalanceConfirmation={}, warnings={}",
                result.getClientId(),
                result.getPaymentAmount(),
                result.getTotalAllocated(),
                result.getRemainingUnallocated(),
                result.isRequiresBalanceConfirmation(),
                result.getWarnings() != null ? result.getWarnings().size() : 0
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Registers (commits) a payment and allocations exactly as confirmed.
     *
     * <p>
     * This endpoint is the authoritative commit phase:
     * <ul>
     *   <li>Persists payment header</li>
     *   <li>Persists allocations as provided</li>
     *   <li>Optionally moves remainder to balance (only if explicitly requested)</li>
     *   <li>Must be idempotent via {@code idempotencyKey}</li>
     * </ul>
     * </p>
     *
     * <p>
     * Return value:
     * <ul>
     *   <li><strong>204 No Content</strong> on success (including idempotent replays)</li>
     * </ul>
     * </p>
     *
     * @param request confirmed payment registration command
     * @return 204 No Content
     */
    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterPaymentRequest request) {

        Objects.requireNonNull(request, "request must not be null");

        // Don’t log the whole key; it’s a token that users may reuse for retries.
        String keySafe = safeKey(request.getIdempotencyKey());

        log.info(
                "Payment register requested: clientId={}, amount={}, allocations={}, moveRemainderToBalance={}, idempotencyKey={}",
                request.getClientId(),
                request.getPaymentAmount(),
                request.getAllocations() != null ? request.getAllocations().size() : 0,
                request.isMoveRemainderToBalance(),
                keySafe
        );

        paymentService.registerPayment(request);

        log.info(
                "Payment register completed: clientId={}, amount={}, idempotencyKey={}",
                request.getClientId(),
                request.getPaymentAmount(),
                keySafe
        );

        return ResponseEntity.noContent().build();
    }

    /**
     * Maps domain/validation failures to HTTP 400.
     * If you already have a global @ControllerAdvice, you can remove this safely.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Payment request rejected: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    private static String safeKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return "<null>";
        }
        int keep = Math.min(6, idempotencyKey.length());
        return idempotencyKey.substring(0, keep) + "…";
    }
}
