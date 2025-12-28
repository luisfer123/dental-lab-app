package com.dentallab.domain.pricing.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;
import com.dentallab.domain.pricing.model.BasePriceResult;
import com.dentallab.domain.pricing.model.PriceResolution;
import com.dentallab.domain.pricing.model.PriceResolutionRequest;
import com.dentallab.domain.pricing.service.WorkBasePriceService;
import com.dentallab.domain.pricing.service.WorkPricingService;
import com.dentallab.persistence.entity.BridgeToothEntity;
import com.dentallab.persistence.entity.BridgeWorkEntity;
import com.dentallab.persistence.entity.ClientEntity;
import com.dentallab.persistence.entity.CrownWorkEntity;
import com.dentallab.persistence.entity.ToothRefEntity;
import com.dentallab.persistence.entity.WorkEntity;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkItemPriceOverrideEntity;
import com.dentallab.persistence.entity.WorkOrderEntity;
import com.dentallab.persistence.entity.WorkPriceEntity;
import com.dentallab.persistence.entity.WorkTypePriceEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PricingIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private WorkBasePriceService basePriceService;

    @Autowired
    private WorkPricingService pricingService;

    @Test
    void fullPricingLifecycle_worksEndToEnd() {

        // ------------------------------------------------------------
        // 1. Lookup data
        // ------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        entityManager.persist(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("CROWN");
        type.setLabel("Crown");
        type.setFamily(family); // REQUIRED
        entityManager.persist(type);

        // ------------------------------------------------------------
        // 2. Client & order
        // ------------------------------------------------------------
        ClientEntity client = new ClientEntity();
        client.setDisplayName("Test Client");
        client.setFirstName("Test");
        client.setLastName("Client");
        client.setPrimaryEmail("test.client@lab.local"); // if required
        entityManager.persist(client);

        WorkOrderEntity order = new WorkOrderEntity();
        order.setClient(client);
        order.setDueDate(LocalDateTime.now());
        entityManager.persist(order);

        // ------------------------------------------------------------
        // 3. Work
        // ------------------------------------------------------------
        WorkEntity work = new WorkEntity();
        work.setWorkFamily(family);
        work.setType(type);
        work.setClient(client);
        work.setOrder(order);
        entityManager.persist(work);
        entityManager.flush();

        Long workId = work.getId();
        
        CrownWorkEntity crown = new CrownWorkEntity();
        crown.setWork(work);
        crown.setToothNumber("11");
        crown.setConstitution(FixProstheticConstitution.MONOLITHIC);
        crown.setBuildingTechnique(BuildingTechnique.DIGITAL);
        crown.setCoreMaterialId(1L);

        entityManager.persist(crown);
        entityManager.flush();


        // ------------------------------------------------------------
        // 4. Pricing rule
        // ------------------------------------------------------------
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setWorkFamily("FIXED_PROSTHESIS");
        rule.setWorkType("CROWN");
        rule.setPriceGroup("DEFAULT");
        rule.setBasePrice(new BigDecimal("100.00"));
        rule.setCurrency("MXN");
        rule.setValidFrom(LocalDate.of(2020, 1, 1));
        entityManager.persist(rule);

        // ------------------------------------------------------------
        // 5. Preview base price
        // ------------------------------------------------------------
        BasePriceResult preview =
                basePriceService.previewBasePrice(
                        new PriceResolutionRequest(workId, LocalDate.now(), "DEFAULT")
                );

        assertEquals(0, preview.getBasePrice().compareTo(new BigDecimal("100.00")));

        // ------------------------------------------------------------
        // 6. Fix base price
        // ------------------------------------------------------------
        basePriceService.fixBasePrice(workId, preview);

        // ------------------------------------------------------------
        // 7. Override
        // ------------------------------------------------------------
        WorkPriceEntity workPrice = entityManager.createQuery(
                "select wp from WorkPriceEntity wp where wp.work.id = :id",
                WorkPriceEntity.class
        ).setParameter("id", workId)
         .getSingleResult();

        WorkItemPriceOverrideEntity override = new WorkItemPriceOverrideEntity();
        override.setWorkPriceId(workPrice.getPriceId());
        override.setAdjustment(new BigDecimal("25.00"));
        override.setReason("Urgent case");
        entityManager.persist(override);

        // ------------------------------------------------------------
        // 8. Final price
        // ------------------------------------------------------------
        PriceResolution finalPrice =
                pricingService.resolveFinalPrice(
                        PriceResolutionRequest.forWork(workId)
                );

        assertEquals(0, finalPrice.getBasePrice().compareTo(new BigDecimal("100.00")));
        assertEquals(0, finalPrice.getTotalOverrides().compareTo(new BigDecimal("25.00")));
        assertEquals(0, finalPrice.getFinalPrice().compareTo(new BigDecimal("125.00")));
    }
    
    @Test
    void bridgePricing_usesProstheticUnitsCorrectly() {

        // ------------------------------------------------------------
        // 1. Lookup data
        // ------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        entityManager.persist(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("BRIDGE");
        type.setLabel("Bridge");
        type.setFamily(family);
        entityManager.persist(type);

        // ------------------------------------------------------------
        // 2. Client & order
        // ------------------------------------------------------------
        ClientEntity client = new ClientEntity();
        client.setDisplayName("Test Client");
        client.setFirstName("Test");
        client.setLastName("Client");
        client.setPrimaryEmail("test.client@lab.local");
        entityManager.persist(client);

        WorkOrderEntity order = new WorkOrderEntity();
        order.setClient(client);
        order.setDueDate(LocalDateTime.now());
        entityManager.persist(order);

        // ------------------------------------------------------------
        // 3. Work
        // ------------------------------------------------------------
        WorkEntity work = new WorkEntity();
        work.setWorkFamily(family);
        work.setType(type);
        work.setClient(client);
        work.setOrder(order);
        entityManager.persist(work);
        entityManager.flush();

        Long workId = work.getId();

        // ------------------------------------------------------------
        // 4. Bridge extension (3-unit bridge)
        // ------------------------------------------------------------
        BridgeWorkEntity bridge = new BridgeWorkEntity();
        bridge.setWork(work);
        bridge.setConstitution(FixProstheticConstitution.STRATIFIED);
        bridge.setBuildingTechnique(BuildingTechnique.DIGITAL);
        bridge.setCoreMaterialId(1L);
        entityManager.persist(bridge);

        ToothRefEntity tooth11 = new ToothRefEntity();
        tooth11.setId(11L);
        tooth11.setToothNumber("11");
        entityManager.persist(tooth11);

        ToothRefEntity tooth12 = new ToothRefEntity();
        tooth12.setId(12L);
        tooth12.setToothNumber("12");
        entityManager.persist(tooth12);

        ToothRefEntity tooth13 = new ToothRefEntity();
        tooth13.setId(13L);
        tooth13.setToothNumber("13");
        entityManager.persist(tooth13);

        entityManager.persist(new BridgeToothEntity(
                bridge, tooth11, BridgeToothEntity.Role.ABUTMENT, 1
        ));
        entityManager.persist(new BridgeToothEntity(
                bridge, tooth12, BridgeToothEntity.Role.PONTIC, 2
        ));
        entityManager.persist(new BridgeToothEntity(
                bridge, tooth13, BridgeToothEntity.Role.ABUTMENT, 3
        ));

        entityManager.flush();

        // ------------------------------------------------------------
        // 5. Pricing rule (price per unit)
        // ------------------------------------------------------------
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setWorkFamily("FIXED_PROSTHESIS");
        rule.setWorkType("CROWN");
        rule.setPriceGroup("DEFAULT");
        rule.setPricePerUnit(new BigDecimal("50.00"));
        rule.setCurrency("MXN");
        rule.setValidFrom(LocalDate.of(2020, 1, 1));
        entityManager.persist(rule);

        // ------------------------------------------------------------
        // 6. Preview base price
        // ------------------------------------------------------------
        BasePriceResult preview =
                basePriceService.previewBasePrice(
                        new PriceResolutionRequest(workId, LocalDate.now(), "DEFAULT")
                );

        assertEquals(
                0,
                preview.getBasePrice().compareTo(new BigDecimal("150.00")),
                "3 units × 50.00 should equal 150.00"
        );

        // ------------------------------------------------------------
        // 7. Fix base price
        // ------------------------------------------------------------
        basePriceService.fixBasePrice(workId, preview);

        // ------------------------------------------------------------
        // 8. Override
        // ------------------------------------------------------------
        WorkPriceEntity workPrice = entityManager.createQuery(
                "select wp from WorkPriceEntity wp where wp.work.id = :id",
                WorkPriceEntity.class
        ).setParameter("id", workId)
         .getSingleResult();

        WorkItemPriceOverrideEntity override = new WorkItemPriceOverrideEntity();
        override.setWorkPriceId(workPrice.getPriceId());
        override.setAdjustment(new BigDecimal("25.00"));
        override.setReason("Urgent case");
        entityManager.persist(override);

        // ------------------------------------------------------------
        // 9. Final price
        // ------------------------------------------------------------
        PriceResolution finalPrice =
                pricingService.resolveFinalPrice(
                        PriceResolutionRequest.forWork(workId)
                );

        assertEquals(
                0,
                finalPrice.getBasePrice().compareTo(new BigDecimal("150.00"))
        );
        assertEquals(
                0,
                finalPrice.getTotalOverrides().compareTo(new BigDecimal("25.00"))
        );
        assertEquals(
                0,
                finalPrice.getFinalPrice().compareTo(new BigDecimal("175.00"))
        );
    }
    
    @Test
    void bridgePricing_usesBasePriceWhenDefined() {

        // ------------------------------------------------------------
        // 1. Lookup data
        // ------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        entityManager.persist(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("BRIDGE");
        type.setLabel("Bridge");
        type.setFamily(family);
        entityManager.persist(type);

        // ------------------------------------------------------------
        // 2. Client & order
        // ------------------------------------------------------------
        ClientEntity client = new ClientEntity();
        client.setDisplayName("Test Client");
        client.setFirstName("Test");
        client.setLastName("Client");
        client.setPrimaryEmail("test.client@lab.local");
        entityManager.persist(client);

        WorkOrderEntity order = new WorkOrderEntity();
        order.setClient(client);
        order.setDueDate(LocalDateTime.now());
        entityManager.persist(order);

        // ------------------------------------------------------------
        // 3. Work
        // ------------------------------------------------------------
        WorkEntity work = new WorkEntity();
        work.setWorkFamily(family);
        work.setType(type);
        work.setClient(client);
        work.setOrder(order);
        entityManager.persist(work);
        entityManager.flush();

        Long workId = work.getId();

        // ------------------------------------------------------------
        // 4. Bridge extension (3-unit bridge)
        // ------------------------------------------------------------
        BridgeWorkEntity bridge = new BridgeWorkEntity();
        bridge.setWork(work);
        bridge.setConstitution(FixProstheticConstitution.STRATIFIED);
        bridge.setBuildingTechnique(BuildingTechnique.DIGITAL);
        bridge.setCoreMaterialId(1L);
        entityManager.persist(bridge);

        ToothRefEntity tooth11 = new ToothRefEntity();
        tooth11.setId(11L);
        tooth11.setToothNumber("11");
        entityManager.persist(tooth11);

        ToothRefEntity tooth12 = new ToothRefEntity();
        tooth12.setId(12L);
        tooth12.setToothNumber("12");
        entityManager.persist(tooth12);

        ToothRefEntity tooth13 = new ToothRefEntity();
        tooth13.setId(13L);
        tooth13.setToothNumber("13");
        entityManager.persist(tooth13);

        entityManager.persist(new BridgeToothEntity(
                bridge, tooth11, BridgeToothEntity.Role.ABUTMENT, 1
        ));
        entityManager.persist(new BridgeToothEntity(
                bridge, tooth12, BridgeToothEntity.Role.PONTIC, 2
        ));
        entityManager.persist(new BridgeToothEntity(
                bridge, tooth13, BridgeToothEntity.Role.ABUTMENT, 3
        ));

        entityManager.flush();

        // ------------------------------------------------------------
        // 5. Pricing rule (BASE PRICE, not per unit)
        // ------------------------------------------------------------
        WorkTypePriceEntity rule = new WorkTypePriceEntity();
        rule.setWorkFamily("FIXED_PROSTHESIS");
        rule.setWorkType("CROWN"); // flattened pricing identity
        rule.setPriceGroup("DEFAULT");
        rule.setBasePrice(new BigDecimal("400.00")); // flat bridge price
        rule.setCurrency("MXN");
        rule.setValidFrom(LocalDate.of(2020, 1, 1));
        entityManager.persist(rule);

        // ------------------------------------------------------------
        // 6. Preview base price
        // ------------------------------------------------------------
        BasePriceResult preview =
                basePriceService.previewBasePrice(
                        new PriceResolutionRequest(workId, LocalDate.now(), "DEFAULT")
                );

        assertEquals(
                0,
                preview.getBasePrice().compareTo(new BigDecimal("400.00")),
                "Base price must be used even if prostheticUnits > 1"
        );

        // ------------------------------------------------------------
        // 7. Fix base price
        // ------------------------------------------------------------
        basePriceService.fixBasePrice(workId, preview);

        // ------------------------------------------------------------
        // 8. Final price (no overrides)
        // ------------------------------------------------------------
        PriceResolution finalPrice =
                pricingService.resolveFinalPrice(
                        PriceResolutionRequest.forWork(workId)
                );

        assertEquals(
                0,
                finalPrice.getBasePrice().compareTo(new BigDecimal("400.00"))
        );
        assertEquals(
                0,
                finalPrice.getTotalOverrides().compareTo(BigDecimal.ZERO)
        );
        assertEquals(
                0,
                finalPrice.getFinalPrice().compareTo(new BigDecimal("400.00"))
        );
    }


}
