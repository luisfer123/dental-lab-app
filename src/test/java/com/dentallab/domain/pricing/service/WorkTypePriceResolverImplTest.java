package com.dentallab.domain.pricing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.model.WorkPricingView;
import com.dentallab.domain.pricing.query.WorkPricingQuery;
import com.dentallab.domain.pricing.query.WorkTypePriceQuery;
import com.dentallab.persistence.entity.WorkTypePriceEntity;

class WorkTypePriceResolverImplTest {

    @Mock
    private WorkPricingQuery workPricingQuery;

    @Mock
    private WorkTypePriceQuery workTypePriceQuery;

    private WorkTypePriceResolverImpl resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new WorkTypePriceResolverImpl(
                workPricingQuery,
                workTypePriceQuery
        );
    }

    @Test
    void resolvesBasePrice_whenExactRuleWithBasePriceExists() {

        Long workId = 1L;
        LocalDate pricingDate = LocalDate.of(2024, 1, 1);

        // --- Given: pricing view ---
        WorkPricingView view = mockPricingView(workId);

        when(workPricingQuery.findByWorkId(workId))
                .thenReturn(view);

        // --- Given: pricing rule ---
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setPriceId(100L);
        rule.setBasePrice(new BigDecimal("150.00"));
        rule.setCurrency("MXN");
        rule.setPriceGroup("DEFAULT");

        when(workTypePriceQuery.findBestMatch(
                view.getWorkFamily(),
                view.getWorkType(),
                "DEFAULT",
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                pricingDate
        )).thenReturn(Optional.of(rule));

        PriceResolutionRequest request =
                new PriceResolutionRequest(
                        workId,
                        pricingDate,
                        "DEFAULT"
                );

        // --- When ---
        BasePriceResult result = resolver.resolveBasePrice(request);

        // --- Then ---
        assertEquals(0, result.getBasePrice().compareTo(new BigDecimal("150.00")));
        assertEquals("MXN", result.getCurrency());
        assertEquals("DEFAULT", result.getPriceGroup());
        assertEquals(100L, result.getWorkTypePriceId());
    }
    
    @Test
    void resolvesBasePrice_whenRuleUsesPricePerUnit() {

        Long workId = 1L;
        LocalDate pricingDate = LocalDate.of(2024, 1, 1);

        // --- Given: pricing view with multiple prosthetic units ---
        WorkPricingView view = new WorkPricingView() {
            @Override public Long getWorkId() { return workId; }
            @Override public String getWorkFamily() { return "FIXED_PROSTHESIS"; }
            @Override public String getWorkType() { return "BRIDGE"; }
            @Override public String getConstitution() { return "STRATIFIED"; }
            @Override public String getBuildingTechnique() { return "DIGITAL"; }
            @Override public Long getCoreMaterialId() { return 1L; }
            @Override public Integer getProstheticUnits() { return 3; }
        };

        when(workPricingQuery.findByWorkId(workId))
                .thenReturn(view);

        // --- Given: pricing rule with price_per_unit ---
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setPriceId(200L);
        rule.setPricePerUnit(new BigDecimal("50.00"));
        rule.setCurrency("MXN");
        rule.setPriceGroup("DEFAULT");

        when(workTypePriceQuery.findBestMatch(
                view.getWorkFamily(),
                view.getWorkType(),
                "DEFAULT",
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                pricingDate
        )).thenReturn(Optional.of(rule));

        PriceResolutionRequest request =
                new PriceResolutionRequest(
                        workId,
                        pricingDate,
                        "DEFAULT"
                );

        // --- When ---
        BasePriceResult result = resolver.resolveBasePrice(request);

        // --- Then ---
        assertEquals(0, result.getBasePrice().compareTo(new BigDecimal("150.00")));
        assertEquals("MXN", result.getCurrency());
        assertEquals("DEFAULT", result.getPriceGroup());
        assertEquals(200L, result.getWorkTypePriceId());
    }

    @Test
    void throwsException_whenNoPricingRuleFound() {

        Long workId = 1L;
        LocalDate pricingDate = LocalDate.of(2024, 1, 1);

        WorkPricingView view = mockPricingView(workId);

        when(workPricingQuery.findByWorkId(workId))
                .thenReturn(view);

        when(workTypePriceQuery.findBestMatch(
                view.getWorkFamily(),
                view.getWorkType(),
                "DEFAULT",
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                pricingDate
        )).thenReturn(Optional.empty());

        PriceResolutionRequest request =
                new PriceResolutionRequest(
                        workId,
                        pricingDate,
                        "DEFAULT"
                );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolveBasePrice(request)
        );

        // Optional sanity check on message
        assertTrue(ex.getMessage().contains("No work_type_price rule"));
    }

    @Test
    void throwsException_whenRuleIsInvalid() {

        Long workId = 1L;
        LocalDate pricingDate = LocalDate.of(2024, 1, 1);

        WorkPricingView view = mockPricingView(workId);

        when(workPricingQuery.findByWorkId(workId))
                .thenReturn(view);

        WorkTypePriceEntity invalidRule = new WorkTypePriceEntity();
        invalidRule.setPriceId(300L);
        invalidRule.setCurrency("MXN");
        invalidRule.setPriceGroup("DEFAULT");
        // basePrice and pricePerUnit intentionally null

        when(workTypePriceQuery.findBestMatch(
                view.getWorkFamily(),
                view.getWorkType(),
                "DEFAULT",
                view.getConstitution(),
                view.getBuildingTechnique(),
                view.getCoreMaterialId(),
                pricingDate
        )).thenReturn(Optional.of(invalidRule));

        PriceResolutionRequest request =
                new PriceResolutionRequest(
                        workId,
                        pricingDate,
                        "DEFAULT"
                );

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> resolver.resolveBasePrice(request)
        );

        assertTrue(ex.getMessage().contains("Invalid"));
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private WorkPricingView mockPricingView(Long workId) {
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
}
