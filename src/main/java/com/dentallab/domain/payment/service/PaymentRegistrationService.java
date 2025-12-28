package com.dentallab.domain.payment.service;

import com.dentallab.domain.payment.model.PaymentAllocationRequest;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRegistrationService {

    /**
     * Registers a payment and applies allocations atomically.
     *
     * @param clientId client making the payment
     * @param paymentAmount total payment amount
     * @param allocations intended allocations per work
     * @param method payment method (cash, transfer, etc.)
     * @param reference optional external reference
     * @param notes optional notes
     *
     * @return paymentId of the created payment
     */
    Long registerPayment(Long clientId,
                         BigDecimal paymentAmount,
                         List<PaymentAllocationRequest> allocations,
                         String method,
                         String reference,
                         String notes);
}



//                     PREVIEW INTERACTION DIAGRAM (Authoritative)
//                     Describes only preview flow without registration
//
//
//							┌──────────────────────────────┐
//							│        Controller / UI        │
//							│  (Payment Preview / Submit)   │
//							└──────────────┬───────────────┘
//							               │
//							               ▼
//							┌──────────────────────────────────────────┐
//							│        PaymentPreviewService              │
//							│  (domain.payment.service)                 │
//							└──────────────┬───────────────┬───────────┘
//							               │               │
//							               │               ▼
//							               │     ┌─────────────────────┐
//							               │     │  ClientBalanceQuery  │
//							               │     │ (domain.payment.query)│
//							               │     └──────────▲──────────┘
//							               │                │
//							               │                │
//							               │     ┌──────────┴──────────┐
//							               │     │ JpaClientBalanceQuery│
//							               │     │ (adapter)             │
//							               │     └──────────▲──────────┘
//							               │                │
//							               │                ▼
//							               │      ClientBalanceRepository
//							               │      (JPA, cache table)
//							               │
//							               ▼
//							┌──────────────────────────────────────────┐
//							│        WorkBalanceProjection              │
//							│  (domain.payment.service)                 │
//							└──────────────┬───────────────┬───────────┘
//							               │               │
//							               │               ▼
//							               │     ┌─────────────────────┐
//							               │     │  WorkBalanceQuery    │
//							               │     │ (domain.payment.query)│
//							               │     └──────────▲──────────┘
//							               │                │
//							               │     ┌──────────┴──────────┐
//							               │     │ JpaWorkBalanceQuery  │
//							               │     │ (adapter)             │
//							               │     └──────────▲──────────┘
//							               │                │
//							               │                ▼
//							               │   PaymentAllocationRepository
//							               │   (SUM allocations per work)
//							               │
//							               ▼
//							┌──────────────────────────────────────────┐
//							│            PriceResolver                  │
//							│      (domain.pricing.service)             │
//							└──────────────┬───────────────┬───────────┘
//							               │               │
//							               │               ▼
//							               │     WorkPricingQuery (port)
//							               │               │
//							               │     WorkPriceQuery / OverrideQuery
//							               │               │
//							               ▼               ▼
//							     Pricing persistence adapters (JPA / SQL)


// -------------------------------------------------------------------------------------------------------------------

//								 PAYMENT REGISTRATION FLOW (Transactional)
//
//									┌──────────────────────────────┐
//									│        Controller / UI        │
//									│     (Confirm Payment)         │
//									└──────────────┬───────────────┘
//									               │
//									               ▼
//									┌──────────────────────────────────────────┐
//									│     PaymentRegistrationService            │
//									│  (domain.payment.service)                 │
//									│  @Transactional                           │
//									└──────────────┬───────────────┬───────────┘
//									               │               │
//									               │               ▼
//									               │     PaymentPreviewService
//									               │     (re-run preview inside TX)
//									               │
//									               ▼
//									┌──────────────────────────────┐
//									│        PaymentRepository      │
//									│      (save PaymentEntity)     │
//									└──────────────┬───────────────┘
//									               │
//									               ▼
//									┌──────────────────────────────┐
//									│   PaymentAllocationRepository │
//									│   (save allocations)          │
//									└──────────────┬───────────────┘
//									               │
//									               ▼
//									┌──────────────────────────────┐
//									│ ClientBalanceMovementRepository│
//									│ (append ledger rows)           │
//									└──────────────┬───────────────┘
//									               │
//									               ▼
//									┌──────────────────────────────┐
//									│   ClientBalanceRepository     │
//									│   (update cache)              │
//									└──────────────────────────────┘
