package com.dentallab.domain.payment.service;

import com.dentallab.domain.payment.dto.PaymentPreviewRequest;
import com.dentallab.domain.payment.dto.PaymentPreviewResult;

/**
 * <p>
 * Service responsible for computing a read-only preview
 * of a payment registration.
 * </p>
 * <p>
 * IMPORTANT: This service provides a non-authoritative preview only.
 * All invariants are re-validated during {@link PaymentService#registerPayment}.
 * Callers must never assume preview results are binding.
 * </p>
 */
public interface PaymentPreviewService {

	/**
	 
	 */

    PaymentPreviewResult preview(PaymentPreviewRequest request);
}
