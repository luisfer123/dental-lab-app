package com.dentallab.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dentallab.domain.payment.dto.PaymentPreviewRequest;
import com.dentallab.domain.payment.dto.PaymentPreviewResult;
import com.dentallab.domain.payment.query.WorkPaymentStatusQuery;
import com.dentallab.domain.payment.service.impl.PaymentPreviewServiceImpl;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkPricingService;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.repository.WorkRepository;

@ExtendWith(MockitoExtension.class)
class PaymentPreviewServiceTest {

    @Mock
    WorkRepository workRepository;

    @Mock
    WorkPricingService workPricingService;

    @Mock
    WorkPaymentStatusQuery paymentStatusQuery;

    @InjectMocks
    PaymentPreviewServiceImpl service;

    @Test
    void preview_allocates_payment_sequentially_with_prior_payments() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        WorkEntity work1 = work(1L);
        WorkEntity work2 = work(2L);

        when(workRepository.findByIdInAndClient_Id(List.of(1L, 2L), clientId))
            .thenReturn(List.of(work1, work2));

        stubPricing(Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("200.00")
        ));

        stubPaidAmounts(Map.of(
            1L, new BigDecimal("50.00")
        ));

        PaymentPreviewRequest request =
            previewRequest(clientId, "150.00", List.of(1L, 2L));

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getTotalUnpaidSelected())
            .isEqualByComparingTo("250.00");

        assertThat(result.getTotalAllocated())
            .isEqualByComparingTo("150.00");

        assertThat(result.getRemainingUnallocated())
            .isEqualByComparingTo("0.00");

        assertThat(result.getWorkAllocations()).hasSize(2);

        assertThat(result.getWorkAllocations().get(0).getWorkId())
            .isEqualTo(1L);
        assertThat(result.getWorkAllocations().get(0).getAllocatedAmount())
            .isEqualByComparingTo("50.00");

        assertThat(result.getWorkAllocations().get(1).getWorkId())
            .isEqualTo(2L);
        assertThat(result.getWorkAllocations().get(1).getAllocatedAmount())
            .isEqualByComparingTo("100.00");
    }
    
    @Test
    void preview_overpayment_requires_balance_confirmation() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        WorkEntity work1 = work(1L);
        WorkEntity work2 = work(2L);

        when(workRepository.findByIdInAndClient_Id(List.of(1L, 2L), clientId))
            .thenReturn(List.of(work1, work2));

        // Prices
        stubPricing(Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("200.00")
        ));

        // No prior payments
        stubPaidAmounts(Map.of());

        // Payment exceeds total unpaid (300)
        PaymentPreviewRequest request =
            previewRequest(clientId, "400.00", List.of(1L, 2L));

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getTotalUnpaidSelected())
            .isEqualByComparingTo("300.00");

        assertThat(result.getTotalAllocated())
            .isEqualByComparingTo("300.00");

        assertThat(result.getRemainingUnallocated())
            .isEqualByComparingTo("100.00");

        assertThat(result.isRequiresBalanceConfirmation())
            .isTrue();

        assertThat(result.getWorkAllocations()).hasSize(2);

        // Work 1 fully paid
        assertThat(result.getWorkAllocations().get(0).getWorkId())
            .isEqualTo(1L);
        assertThat(result.getWorkAllocations().get(0).getAllocatedAmount())
            .isEqualByComparingTo("100.00");

        // Work 2 fully paid
        assertThat(result.getWorkAllocations().get(1).getWorkId())
            .isEqualTo(2L);
        assertThat(result.getWorkAllocations().get(1).getAllocatedAmount())
            .isEqualByComparingTo("200.00");
    }
    
    @Test
    void preview_exact_payment_fully_allocates_without_balance() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        WorkEntity work1 = work(1L);
        WorkEntity work2 = work(2L);

        when(workRepository.findByIdInAndClient_Id(List.of(1L, 2L), clientId))
            .thenReturn(List.of(work1, work2));

        // Prices
        stubPricing(Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("200.00")
        ));

        // No prior payments
        stubPaidAmounts(Map.of());

        // Payment exactly equals total unpaid (300)
        PaymentPreviewRequest request =
            previewRequest(clientId, "300.00", List.of(1L, 2L));

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getTotalUnpaidSelected())
            .isEqualByComparingTo("300.00");

        assertThat(result.getTotalAllocated())
            .isEqualByComparingTo("300.00");

        assertThat(result.getRemainingUnallocated())
            .isEqualByComparingTo("0.00");

        assertThat(result.isRequiresBalanceConfirmation())
            .isFalse();

        assertThat(result.getWorkAllocations()).hasSize(2);

        // Work 1 fully paid
        assertThat(result.getWorkAllocations().get(0).getWorkId())
            .isEqualTo(1L);
        assertThat(result.getWorkAllocations().get(0).getAllocatedAmount())
            .isEqualByComparingTo("100.00");

        // Work 2 fully paid
        assertThat(result.getWorkAllocations().get(1).getWorkId())
            .isEqualTo(2L);
        assertThat(result.getWorkAllocations().get(1).getAllocatedAmount())
            .isEqualByComparingTo("200.00");
    }
    
    @Test
    void preview_respects_selected_work_order_not_repository_order() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        WorkEntity work1 = work(1L);
        WorkEntity work2 = work(2L);

        // Repository returns works in reverse order
        when(workRepository.findByIdInAndClient_Id(List.of(2L, 1L), clientId))
            .thenReturn(List.of(work2, work1));

        stubPricing(Map.of(
            1L, new BigDecimal("100.00"),
            2L, new BigDecimal("200.00")
        ));

        stubPaidAmounts(Map.of());

        // User explicitly selects order: [2, 1]
        PaymentPreviewRequest request =
            previewRequest(clientId, "150.00", List.of(2L, 1L));

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getWorkAllocations()).hasSize(2);

        // First allocation must go to work 2
        assertThat(result.getWorkAllocations().get(0).getWorkId())
            .isEqualTo(2L);
        assertThat(result.getWorkAllocations().get(0).getAllocatedAmount())
            .isEqualByComparingTo("150.00");

        // Second work receives nothing
        assertThat(result.getWorkAllocations().get(1).getWorkId())
            .isEqualTo(1L);
        assertThat(result.getWorkAllocations().get(1).getAllocatedAmount())
            .isEqualByComparingTo("0.00");
    }
    
    @Test
    void preview_with_empty_selection_allocates_nothing() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        when(workRepository.findByIdInAndClient_Id(List.of(), clientId))
            .thenReturn(List.of());

        PaymentPreviewRequest request =
            previewRequest(clientId, "100.00", List.of());

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getTotalUnpaidSelected())
            .isEqualByComparingTo("0.00");

        assertThat(result.getTotalAllocated())
            .isEqualByComparingTo("0.00");

        assertThat(result.getRemainingUnallocated())
            .isEqualByComparingTo("100.00");

        assertThat(result.isRequiresBalanceConfirmation())
            .isTrue();

        assertThat(result.getWorkAllocations())
            .isEmpty();
    }
    
    @Test
    void preview_with_zero_payment_is_rejected() {

        PaymentPreviewRequest request =
            previewRequest(10L, "0.00", List.of(1L));

        assertThatThrownBy(() -> service.preview(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payment amount");
    }

    @Test
    void preview_with_negative_payment_is_rejected() {

        PaymentPreviewRequest request =
            previewRequest(10L, "-50.00", List.of(1L));

        assertThatThrownBy(() -> service.preview(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("payment amount");
    }

    // ----------------------------------------------------
    // Helper methods
    // ----------------------------------------------------

    private WorkEntity work(Long id) {
        WorkEntity work = new WorkEntity();
        work.setId(id);
        return work;
    }

    private PaymentPreviewRequest previewRequest(
            Long clientId,
            String amount,
            List<Long> workIds) {

        PaymentPreviewRequest req = new PaymentPreviewRequest();
        req.setClientId(clientId);
        req.setPaymentAmount(new BigDecimal(amount));
        req.setSelectedWorkIds(workIds);
        return req;
    }

    private void stubPricing(Map<Long, BigDecimal> pricesByWorkId) {
        when(workPricingService.resolveFinalPrice(any(PriceResolutionRequest.class)))
            .thenAnswer(invocation -> {
                PriceResolutionRequest req = invocation.getArgument(0);
                BigDecimal price = pricesByWorkId.get(req.getWorkId());

                if (price == null) {
                    throw new IllegalStateException(
                        "No price stubbed for workId " + req.getWorkId());
                }

                return new PriceResolution(
                    price,
                    BigDecimal.ZERO,
                    price,
                    "MXN",
                    null,
                    null,
                    List.of()
                );
            });
    }

    private void stubPaidAmounts(Map<Long, BigDecimal> cashPaidByWorkId) {

        when(paymentStatusQuery.findCashPaidAmountsByWorkIds(anyList()))
            .thenReturn(cashPaidByWorkId);

        when(paymentStatusQuery.findBalancePaidAmountsByWorkIds(anyList()))
            .thenReturn(Map.of());
    }
}
