package com.dentallab.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dentallab.domain.enums.PaymentStatus;
import com.dentallab.domain.payment.dto.PaymentAllocationCommand;
import com.dentallab.domain.payment.dto.RegisterPaymentRequest;
import com.dentallab.domain.payment.query.WorkPaymentStatusQuery;
import com.dentallab.domain.payment.service.impl.PaymentServiceImpl;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkPricingService;
import com.dentallab.persistence.entity.PaymentEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.PaymentAllocationRepository;
import com.dentallab.persistence.repository.PaymentRepository;
import com.dentallab.persistence.repository.WorkRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock WorkRepository workRepository;
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentAllocationRepository paymentAllocationRepository;
    @Mock WorkPricingService workPricingService;
    @Mock WorkPaymentStatusQuery paymentStatusQuery;
    @Mock ClientBalanceService clientBalanceService;

    @InjectMocks PaymentServiceImpl service;

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private RegisterPaymentRequest baseRequest() {
        RegisterPaymentRequest r = new RegisterPaymentRequest();
        r.setClientId(10L);
        r.setPaymentAmount(new BigDecimal("150.00"));
        r.setIdempotencyKey("abc-123");
        r.setMoveRemainderToBalance(false);
        return r;
    }

    private PaymentAllocationCommand alloc(long workId, String amount) {
        PaymentAllocationCommand a = new PaymentAllocationCommand();
        a.setWorkId(workId);
        a.setAllocatedAmount(new BigDecimal(amount));
        return a;
    }

    private WorkEntity work(long id) {
        WorkEntity w = new WorkEntity();
        w.setId(id);
        return w;
    }

    private void stubWorkLookup(long workId) {
        when(workRepository.findByIdInAndClient_Id(List.of(workId), 10L))
            .thenReturn(List.of(work(workId)));
    }

    private void stubPricing(String finalPrice) {
        when(workPricingService.resolveFinalPrice(any(PriceResolutionRequest.class)))
            .thenReturn(new PriceResolution(
                new BigDecimal(finalPrice),
                BigDecimal.ZERO,
                new BigDecimal(finalPrice),
                "MXN",
                null,
                null,
                List.of()
            ));
    }

    private void stubPaymentStatus(long workId) {
        when(paymentStatusQuery.findCashPaidAmountsByWorkIds(Set.of(workId)))
            .thenReturn(Map.of());

        when(paymentStatusQuery.findBalancePaidAmountsByWorkIds(Set.of(workId)))
            .thenReturn(Map.of());
    }

    // ------------------------------------------------------------------
    // Idempotency
    // ------------------------------------------------------------------

    @Test
    void registerPayment_is_idempotent_and_exits_early() {

        when(paymentRepository.findByIdempotencyKey("abc-123"))
            .thenReturn(Optional.of(new PaymentEntity()));

        service.registerPayment(baseRequest());

        verifyNoInteractions(
            workRepository,
            paymentAllocationRepository,
            clientBalanceService
        );
    }

    // ------------------------------------------------------------------
    // Fully allocated payment
    // ------------------------------------------------------------------

    @Test
    void registerPayment_fully_allocated_without_balance_credit() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "150.00")));

        stubWorkLookup(1L);
        stubPricing("200.00");
        stubPaymentStatus(1L);

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        when(paymentRepository.save(any()))
            .thenAnswer(i -> {
                PaymentEntity p = i.getArgument(0);
                p.setId(99L);
                p.setStatus(PaymentStatus.RECEIVED);
                return p;
            });

        service.registerPayment(req);

        verify(paymentAllocationRepository).save(any());
        verify(clientBalanceService, never())
            .creditBalance(any(), any(), any(), any(), any(), any());
    }

    // ------------------------------------------------------------------
    // Remainder credited to balance
    // ------------------------------------------------------------------

    @Test
    void registerPayment_with_remainder_credits_balance() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "100.00")));
        req.setMoveRemainderToBalance(true);

        stubWorkLookup(1L);
        stubPricing("100.00");
        stubPaymentStatus(1L);

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        when(paymentRepository.save(any()))
            .thenAnswer(i -> {
                PaymentEntity p = i.getArgument(0);
                p.setId(50L);
                return p;
            });

        service.registerPayment(req);

        verify(clientBalanceService).creditBalance(
            eq(10L),
            eq(new BigDecimal("50.00")),
            eq("PAY_EXCESS"),
            eq(50L),
            isNull(),
            anyString()
        );
    }

    // ------------------------------------------------------------------
    // Invalid allocation
    // ------------------------------------------------------------------

    @Test
    void registerPayment_rejects_allocation_exceeding_work_price() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "120.00")));

        stubWorkLookup(1L);
        stubPricing("100.00");   // final price = 100

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerPayment(req))
	        .isInstanceOf(IllegalArgumentException.class)
	        .hasMessageContaining("exceeds work price");

        verify(paymentAllocationRepository, never()).save(any());
        verify(clientBalanceService, never())
            .creditBalance(any(), any(), any(), any(), any(), any());
    }

    // ------------------------------------------------------------------
    // Remainder without confirmation
    // ------------------------------------------------------------------

    @Test
    void registerPayment_rejects_remainder_without_balance_confirmation() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "50.00")));
        req.setMoveRemainderToBalance(false);

        stubWorkLookup(1L);
        stubPricing("50.00");
        stubPaymentStatus(1L);

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerPayment(req))
            .isInstanceOf(IllegalStateException.class);

        verify(clientBalanceService, never())
            .creditBalance(any(), any(), any(), any(), any(), any());
    }

    // ------------------------------------------------------------------
    // Optional: Duplicate allocations rejected
    // ------------------------------------------------------------------

    @Test
    void registerPayment_rejects_duplicate_work_allocations() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(
            alloc(1L, "50.00"),
            alloc(1L, "20.00")
        ));

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerPayment(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Duplicate work allocations");

        verifyNoInteractions(
            workRepository,
            paymentAllocationRepository,
            clientBalanceService
        );
    }

    // ------------------------------------------------------------------
    // Optional: Zero-amount allocation ignored
    // ------------------------------------------------------------------

    @Test
    void registerPayment_ignores_zero_amount_allocations() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "0.00")));
        req.setMoveRemainderToBalance(true);

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        when(paymentRepository.save(any()))
            .thenAnswer(i -> {
                PaymentEntity p = i.getArgument(0);
                p.setId(77L);
                return p;
            });

        // ------------------------------------------------------------
        // Execute
        // ------------------------------------------------------------
        service.registerPayment(req);

        // ------------------------------------------------------------
        // No work-related interactions at all
        // ------------------------------------------------------------
        verifyNoInteractions(
            workRepository,
            workPricingService,
            paymentAllocationRepository,
            paymentStatusQuery
        );

        // ------------------------------------------------------------
        // Entire amount credited to balance
        // ------------------------------------------------------------
        verify(clientBalanceService).creditBalance(
            eq(10L),
            eq(new BigDecimal("150.00")),
            eq("PAY_EXCESS"),
            eq(77L),
            isNull(),
            anyString()
        );
    }

    // ------------------------------------------------------------------
    // Optional: Negative allocation rejected
    // ------------------------------------------------------------------

    @Test
    void registerPayment_rejects_negative_allocations() {

        RegisterPaymentRequest req = baseRequest();
        req.setAllocations(List.of(alloc(1L, "-1.00")));

        when(paymentRepository.findByIdempotencyKey(any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registerPayment(req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Negative allocation");

        verifyNoInteractions(
            workRepository,
            paymentAllocationRepository,
            clientBalanceService
        );
    }
    
}
