package com.dentallab.domain.payment.service;

import com.dentallab.domain.payment.dto.RegisterPaymentRequest;

/**
 * Service responsible for registering real payments
 * and persisting their financial effects.
 *
 * <p>
 * This service represents the <strong>commit phase</strong>
 * of the payment workflow.
 * </p>
 *
 * <p>
 * Unlike preview services, this service:
 * </p>
 *
 * <ul>
 *   <li>persists data</li>
 *   <li>runs inside a transaction</li>
 *   <li>re-validates all invariants</li>
 *   <li>must either fully succeed or fully fail</li>
 * </ul>
 */
public interface PaymentService {

    /**
     * Registers a payment and applies its allocations.
     *
     * @param request confirmed payment registration request
     */
    void registerPayment(RegisterPaymentRequest request);
}
