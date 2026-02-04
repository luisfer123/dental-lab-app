package com.dentallab.domain.pricing.integration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.dentallab.domain.pricing.exception.DuplicateWorkTypePriceRuleException;
import com.dentallab.domain.pricing.model.CreateWorkTypePriceRuleCommand;
import com.dentallab.domain.pricing.service.WorkTypePriceRuleService;
import com.dentallab.persistence.entity.WorkFamilyRefEntity;
import com.dentallab.persistence.entity.WorkTypeRefEntity;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkTypePriceRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkTypePriceRuleIntegrationTest {

    @Autowired
    private WorkTypePriceRuleService service;

    @Autowired
    private WorkFamilyRefRepository workFamilyRefRepository;

    @Autowired
    private WorkTypeRefRepository workTypeRefRepository;
    
    @Autowired
    private WorkTypePriceRepository workTypePriceRepository;

    @Test
    void duplicate_pricing_rule_is_rejected_by_database_constraint() {

        // -----------------------------------------------------------------
        // Arrange: lookup data (real DB rows)
        // -----------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        workFamilyRefRepository.save(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("CROWN");
        type.setLabel("Crown");
        type.setFamily(family);
        workTypeRefRepository.save(type);

        CreateWorkTypePriceRuleCommand cmd =
        	    CreateWorkTypePriceRuleCommand.builder()
        	        .workFamily("FIXED_PROSTHESIS")
        	        .workType("CROWN")
        	        .constitution("MONOLITHIC") // 👈 key change
        	        .basePrice(new BigDecimal("200.00"))
        	        .validFrom(LocalDate.of(2025, 1, 1))
        	        .build();

        // -----------------------------------------------------------------
        // Act: create rule once (should succeed)
        // -----------------------------------------------------------------
        service.createRule(cmd);
        workTypePriceRepository.flush();

        // -----------------------------------------------------------------
        // Assert: second identical rule fails
        // -----------------------------------------------------------------
        assertThatThrownBy(() -> service.createRule(cmd))
            .isInstanceOf(DuplicateWorkTypePriceRuleException.class)
            .hasMessageContaining("pricing rule already exists");
    }
    
    @Test
    void pricing_rules_with_different_constitution_are_allowed() {
    	
    	WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        workFamilyRefRepository.save(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("CROWN");
        type.setLabel("Crown");
        type.setFamily(family);
        workTypeRefRepository.save(type);

        CreateWorkTypePriceRuleCommand mono =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("MONOLITHIC")
                .basePrice(new BigDecimal("200.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        CreateWorkTypePriceRuleCommand strat =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("STRATIFIED")
                .basePrice(new BigDecimal("250.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        service.createRule(mono);
        service.createRule(strat); // should NOT throw
    }
    
    @Test
    void same_pricing_rule_with_different_validFrom_is_allowed() {
    	
    	// -----------------------------------------------------------------
        // Arrange: lookup data (real DB rows)
        // -----------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        workFamilyRefRepository.save(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("CROWN");
        type.setLabel("Crown");
        type.setFamily(family);
        workTypeRefRepository.save(type);
        
        CreateWorkTypePriceRuleCommand jan =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("MONOLITHIC")
                .basePrice(new BigDecimal("200.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        CreateWorkTypePriceRuleCommand feb =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("MONOLITHIC")
                .basePrice(new BigDecimal("220.00"))
                .validFrom(LocalDate.of(2025, 2, 1))
                .build();

        service.createRule(jan);
        service.createRule(feb); // allowed
    }

    @Test
    void null_attributes_are_normalized_for_uniqueness() {
    	
    	// -----------------------------------------------------------------
        // Arrange: lookup data (real DB rows)
        // -----------------------------------------------------------------
        WorkFamilyRefEntity family = new WorkFamilyRefEntity();
        family.setCode("FIXED_PROSTHESIS");
        family.setLabel("Fixed Prosthesis");
        workFamilyRefRepository.save(family);

        WorkTypeRefEntity type = new WorkTypeRefEntity();
        type.setCode("CROWN");
        type.setLabel("Crown");
        type.setFamily(family);
        workTypeRefRepository.save(type);
    	
        CreateWorkTypePriceRuleCommand generic =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .basePrice(new BigDecimal("180.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        CreateWorkTypePriceRuleCommand explicitWildcard =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution(null) // same semantic wildcard
                .basePrice(new BigDecimal("190.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        service.createRule(generic);

        assertThatThrownBy(() -> service.createRule(explicitWildcard))
            .isInstanceOf(DuplicateWorkTypePriceRuleException.class);
    }


}
