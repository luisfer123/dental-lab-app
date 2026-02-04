package com.dentallab.domain.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.pricing.exception.DuplicateWorkTypePriceRuleException;
import com.dentallab.domain.pricing.model.CreateWorkTypePriceRuleCommand;
import com.dentallab.domain.pricing.service.impl.WorkTypePriceRuleServiceImpl;
import com.dentallab.persistence.entity.WorkTypePriceEntity;
import com.dentallab.persistence.repository.WorkFamilyRefRepository;
import com.dentallab.persistence.repository.WorkTypePriceRepository;
import com.dentallab.persistence.repository.WorkTypeRefRepository;

@ExtendWith(MockitoExtension.class)
class WorkTypePriceRuleServiceTest {

    @Mock
    private WorkTypePriceRepository workTypePriceRepository;

    @Mock
    private WorkFamilyRefRepository workFamilyRefRepository;

    @Mock
    private WorkTypeRefRepository workTypeRefRepository;

    @InjectMocks
    private WorkTypePriceRuleServiceImpl service;

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private CreateWorkTypePriceRuleCommand basePriceCommand() {
        return CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("MONOLITHIC")
                .buildingTechnique(BuildingTechnique.DIGITAL)
                .basePrice(new BigDecimal("200.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .notes("Standard crown price")
                .build();
    }

    private CreateWorkTypePriceRuleCommand perUnitCommand() {
        return CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("BRIDGE")
                .pricePerUnit(new BigDecimal("150.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();
    }

    // ---------------------------------------------------------------------
    // Happy paths
    // ---------------------------------------------------------------------

    @Test
    void creates_base_price_rule_successfully() {
    	
    	when(workFamilyRefRepository.existsById(any())).thenReturn(true);
        when(workTypeRefRepository.existsById(any())).thenReturn(true);

        when(workTypePriceRepository.save(any()))
                .thenAnswer(inv -> {
                    WorkTypePriceEntity e = inv.getArgument(0);
                    e.setPriceId(1L);
                    return e;
                });

        WorkTypePriceEntity saved = service.createRule(basePriceCommand());

        assertThat(saved.getPriceId()).isEqualTo(1L);
        assertThat(saved.getBasePrice()).isEqualByComparingTo("200.00");
        assertThat(saved.getPricePerUnit()).isNull();
        assertThat(saved.getPriceGroup()).isEqualTo("DEFAULT");
        assertThat(saved.getCurrency()).isEqualTo("MXN");

        verify(workTypePriceRepository).save(any());
    }

    @Test
    void creates_price_per_unit_rule_successfully() {
    	
    	when(workFamilyRefRepository.existsById(any())).thenReturn(true);
        when(workTypeRefRepository.existsById(any())).thenReturn(true);

        when(workTypePriceRepository.save(any()))
                .thenAnswer(inv -> {
                    WorkTypePriceEntity e = inv.getArgument(0);
                    e.setPriceId(2L);
                    return e;
                });

        WorkTypePriceEntity saved = service.createRule(perUnitCommand());

        assertThat(saved.getPricePerUnit()).isEqualByComparingTo("150.00");
        assertThat(saved.getBasePrice()).isNull();

        verify(workTypePriceRepository).save(any());
    }

    // ---------------------------------------------------------------------
    // Validation failures (no persistence)
    // ---------------------------------------------------------------------

    @Test
    void rejects_null_command() {
        assertThatThrownBy(() -> service.createRule(null))
                .isInstanceOf(NullPointerException.class);

        verifyNoInteractions(workTypePriceRepository);
    }

    @Test
    void rejects_command_with_both_base_price_and_price_per_unit() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .basePrice(new BigDecimal("200.00"))
                .pricePerUnit(new BigDecimal("50.00"))
                .validFrom(LocalDate.now())
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Exactly one of basePrice or pricePerUnit");
    }

    @Test
    void rejects_rule_with_negative_price() {

        assertThatThrownBy(() -> 
                CreateWorkTypePriceRuleCommand.builder()
                        .workFamily("FIXED_PROSTHESIS")
                        .workType("CROWN")
                        .basePrice(new BigDecimal("-1"))
                        .validFrom(LocalDate.now())
                        .build()
        )
		.isInstanceOf(IllegalArgumentException.class)
		.hasMessageContaining("Price must be >=");

        verifyNoInteractions(workTypePriceRepository);
    }

    @Test
    void rejects_unknown_work_family() {

        when(workFamilyRefRepository.existsById(any()))
                .thenReturn(false);

        assertThatThrownBy(() -> service.createRule(basePriceCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown workFamily");

        verifyNoInteractions(workTypePriceRepository);
    }

    @Test
    void rejects_unknown_work_type() {
    	
    	when(workFamilyRefRepository.existsById(any())).thenReturn(true);

        when(workTypeRefRepository.existsById(any()))
                .thenReturn(false);

        assertThatThrownBy(() -> service.createRule(basePriceCommand()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown workType");

        verifyNoInteractions(workTypePriceRepository);
    }

    // ---------------------------------------------------------------------
    // Uniqueness enforcement (DB-driven)
    // ---------------------------------------------------------------------

    @Test
    void translates_db_unique_violation_to_domain_exception() {
    	
    	when(workFamilyRefRepository.existsById(any())).thenReturn(true);
        when(workTypeRefRepository.existsById(any())).thenReturn(true);

        when(workTypePriceRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("uq_work_type_price"));

        assertThatThrownBy(() -> service.createRule(basePriceCommand()))
                .isInstanceOf(DuplicateWorkTypePriceRuleException.class)
                .hasMessageContaining("pricing rule already exists");

        verify(workTypePriceRepository).save(any());
    }
}
