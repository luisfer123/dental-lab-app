package com.dentallab.domain.pricing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.model.WorkPricingView;
import com.dentallab.domain.pricing.query.WorkPriceOverrideQuery;
import com.dentallab.domain.pricing.query.WorkPriceQuery;
import com.dentallab.domain.pricing.query.WorkPricingQuery;
import com.dentallab.persistence.entity.WorkItemPriceOverrideEntity;
import com.dentallab.persistence.entity.WorkPriceEntity;

class FinalWorkPriceResolverImplTest {

    @Mock
    private WorkPricingQuery pricingQuery;

    @Mock
    private WorkPriceQuery workPriceQuery;

    @Mock
    private WorkPriceOverrideQuery overrideQuery;

    private FinalWorkPriceResolverImpl resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new FinalWorkPriceResolverImpl(
                pricingQuery,
                workPriceQuery,
                overrideQuery
        );
    }

    @Test
    void resolvesFinalPrice_whenNoOverrides() {

        Long workId = 1L;

        // --- Given ---
        WorkPricingView pricingView = mockWorkPricingView(workId);
        when(pricingQuery.findByWorkId(workId)).thenReturn(pricingView);

        WorkPriceEntity basePrice = new WorkPriceEntity();
        basePrice.setPriceId(10L);
        basePrice.setPrice(new BigDecimal("100.00"));
        basePrice.setCurrency("MXN");
        basePrice.setPriceGroup("DEFAULT");

        when(workPriceQuery.findByWorkId(workId))
                .thenReturn(Optional.of(basePrice));

        when(overrideQuery.findByWorkPriceId(10L))
                .thenReturn(List.of());

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        // --- When ---
        PriceResolution result = resolver.resolve(request);

        // --- Then ---
        assertEquals(0, result.getBasePrice().compareTo(new BigDecimal("100.00")));
        assertEquals(0, result.getTotalOverrides().compareTo(BigDecimal.ZERO));
        assertEquals(0, result.getFinalPrice().compareTo(new BigDecimal("100.00")));
        assertEquals("MXN", result.getCurrency());
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private WorkPricingView mockWorkPricingView(Long workId) {
        return new WorkPricingView() {
            @Override public Long getWorkId() { return workId; }
            @Override public String getWorkFamily() { return "FIXED_PROSTHESIS"; }
            @Override public String getWorkType() { return "CROWN"; }
            @Override public String getConstitution() { return "MONOLITHIC"; }
            @Override public String getBuildingTechnique() { return "DIGITAL"; }
            @Override public Long getCoreMaterialId() { return 1L; }
            @Override public Integer getProstheticUnits() { return 1; }
        };
    }
    
    @Test
    void resolvesFinalPrice_withSingleOverride() {

        Long workId = 1L;

        // --- Given ---
        WorkPricingView pricingView = mockWorkPricingView(workId);
        when(pricingQuery.findByWorkId(workId)).thenReturn(pricingView);

        WorkPriceEntity basePrice = new WorkPriceEntity();
        basePrice.setPriceId(10L);
        basePrice.setPrice(new BigDecimal("100.00"));
        basePrice.setCurrency("MXN");
        basePrice.setPriceGroup("DEFAULT");

        when(workPriceQuery.findByWorkId(workId))
                .thenReturn(Optional.of(basePrice));

        WorkItemPriceOverrideEntity override = new WorkItemPriceOverrideEntity();
        override.setAdjustment(new BigDecimal("20.00"));

        when(overrideQuery.findByWorkPriceId(10L))
                .thenReturn(List.of(override));

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        // --- When ---
        PriceResolution result = resolver.resolve(request);

        // --- Then ---
        assertEquals(0, result.getBasePrice().compareTo(new BigDecimal("100.00")));
        assertEquals(0, result.getTotalOverrides().compareTo(new BigDecimal("20.00")));
        assertEquals(0, result.getFinalPrice().compareTo(new BigDecimal("120.00")));
        assertEquals("MXN", result.getCurrency());
    }
    
    @Test
    void resolvesFinalPrice_withMultipleOverrides() {

        Long workId = 1L;

        // --- Given ---
        WorkPricingView pricingView = mockWorkPricingView(workId);
        when(pricingQuery.findByWorkId(workId)).thenReturn(pricingView);

        WorkPriceEntity basePrice = new WorkPriceEntity();
        basePrice.setPriceId(10L);
        basePrice.setPrice(new BigDecimal("100.00"));
        basePrice.setCurrency("MXN");
        basePrice.setPriceGroup("DEFAULT");

        when(workPriceQuery.findByWorkId(workId))
                .thenReturn(Optional.of(basePrice));

        WorkItemPriceOverrideEntity override1 = new WorkItemPriceOverrideEntity();
        override1.setAdjustment(new BigDecimal("20.00"));

        WorkItemPriceOverrideEntity override2 = new WorkItemPriceOverrideEntity();
        override2.setAdjustment(new BigDecimal("-10.00"));

        when(overrideQuery.findByWorkPriceId(10L))
                .thenReturn(List.of(override1, override2));

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        // --- When ---
        PriceResolution result = resolver.resolve(request);

        // --- Then ---
        assertEquals(0, result.getBasePrice().compareTo(new BigDecimal("100.00")));
        assertEquals(0, result.getTotalOverrides().compareTo(new BigDecimal("10.00")));
        assertEquals(0, result.getFinalPrice().compareTo(new BigDecimal("110.00")));
        assertEquals("MXN", result.getCurrency());
    }

    @Test
    void throwsException_whenBasePriceMissing() {

        Long workId = 1L;

        when(pricingQuery.findByWorkId(workId))
                .thenReturn(mockWorkPricingView(workId));

        when(workPriceQuery.findByWorkId(workId))
                .thenReturn(Optional.empty());

        PriceResolutionRequest request =
                PriceResolutionRequest.forWork(workId);

        assertThrows(IllegalStateException.class,
                () -> resolver.resolve(request));
    }


}
