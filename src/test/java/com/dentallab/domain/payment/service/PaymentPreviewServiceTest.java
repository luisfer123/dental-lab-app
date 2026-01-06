package com.dentallab.domain.payment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
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
    private WorkRepository workRepository;

    @Mock
    private WorkPricingService workPricingService;

    @Mock
    private WorkPaymentStatusQuery paymentStatusQuery;

    @InjectMocks
    private PaymentPreviewServiceImpl service;

    private WorkEntity work1;
    private WorkEntity work2;

    @BeforeEach
    void setup() {
        work1 = new WorkEntity();
        work1.setId(1L);

        work2 = new WorkEntity();
        work2.setId(2L);
    }

    @Test
    void preview_allocates_payment_in_order_and_computes_remainder() {

        // ----------------------------------------------------
        // Given
        // ----------------------------------------------------
        Long clientId = 10L;

        PaymentPreviewRequest request = new PaymentPreviewRequest();
        request.setClientId(clientId);
        request.setPaymentAmount(new BigDecimal("150.00"));
        request.setSelectedWorkIds(List.of(1L, 2L));

        when(workRepository.findByIdInAndClient_Id(
                List.of(1L, 2L), clientId))
            .thenReturn(List.of(work1, work2));

        // Prices (real constructor)
        when(workPricingService.resolveFinalPrice(
                PriceResolutionRequest.forWork(1L)))
            .thenReturn(new PriceResolution(
                    new BigDecimal("100.00"),   // basePrice
                    BigDecimal.ZERO,            // totalOverrides
                    new BigDecimal("100.00"),   // finalPrice
                    "MXN",
                    null,
                    null,
                    List.of()
            ));

        when(workPricingService.resolveFinalPrice(
                PriceResolutionRequest.forWork(2L)))
            .thenReturn(new PriceResolution(
                    new BigDecimal("200.00"),
                    BigDecimal.ZERO,
                    new BigDecimal("200.00"),
                    "MXN",
                    null,
                    null,
                    List.of()
            ));

        // Already paid
        when(paymentStatusQuery.findCashPaidAmountsByWorkIds(Set.of(1L, 2L)))
            .thenReturn(Map.of(
                    1L, new BigDecimal("20.00"),
                    2L, BigDecimal.ZERO
            ));

        when(paymentStatusQuery.findBalancePaidAmountsByWorkIds(Set.of(1L, 2L)))
            .thenReturn(Map.of());

        // ----------------------------------------------------
        // When
        // ----------------------------------------------------
        PaymentPreviewResult result = service.preview(request);

        // ----------------------------------------------------
        // Then
        // ----------------------------------------------------
        assertThat(result.getTotalUnpaidSelected())
                .isEqualByComparingTo("280.00"); // (100-20) + 200

        assertThat(result.getTotalAllocated())
                .isEqualByComparingTo("150.00");

        assertThat(result.getRemainingUnallocated())
                .isEqualByComparingTo("0.00");

        assertThat(result.isRequiresBalanceConfirmation())
                .isFalse();

        assertThat(result.getWorkAllocations()).hasSize(2);

        // Work 1
        assertThat(result.getWorkAllocations().get(0).getWorkId())
                .isEqualTo(1L);
        assertThat(result.getWorkAllocations().get(0).getAllocatedAmount())
                .isEqualByComparingTo("80.00"); // 100 - 20

        // Work 2
        assertThat(result.getWorkAllocations().get(1).getWorkId())
                .isEqualTo(2L);
        assertThat(result.getWorkAllocations().get(1).getAllocatedAmount())
                .isEqualByComparingTo("70.00"); // remainder
    }
}
