package com.dentallab.domain.pricing.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.dentallab.api.enums.BuildingTechnique;

class CreateWorkTypePriceRuleCommandTest {

    @Test
    void builds_valid_base_price_command() {

        CreateWorkTypePriceRuleCommand cmd =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .constitution("MONOLITHIC")
                .buildingTechnique(BuildingTechnique.DIGITAL)
                .basePrice(new BigDecimal("200.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .notes("Standard crown price")
                .build();

        assertThat(cmd.getWorkFamily()).isEqualTo("FIXED_PROSTHESIS");
        assertThat(cmd.getWorkType()).isEqualTo("CROWN");
        assertThat(cmd.getBasePrice()).isEqualByComparingTo("200.00");
        assertThat(cmd.getPricePerUnit()).isNull();
        assertThat(cmd.getPriceGroup()).isEqualTo("DEFAULT");
        assertThat(cmd.getCurrency()).isEqualTo("MXN");
    }

    @Test
    void builds_valid_price_per_unit_command() {

        CreateWorkTypePriceRuleCommand cmd =
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("BRIDGE")
                .pricePerUnit(new BigDecimal("150.00"))
                .validFrom(LocalDate.of(2025, 1, 1))
                .build();

        assertThat(cmd.getPricePerUnit()).isEqualByComparingTo("150.00");
        assertThat(cmd.getBasePrice()).isNull();
    }

    @Test
    void rejects_both_base_price_and_price_per_unit() {

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
    void rejects_missing_price() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .validFrom(LocalDate.now())
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Exactly one of basePrice or pricePerUnit");
    }

    @Test
    void rejects_negative_base_price() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .basePrice(new BigDecimal("-1"))
                .validFrom(LocalDate.now())
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("basePrice must be >=");
    }

    @Test
    void rejects_missing_work_family() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workType("CROWN")
                .basePrice(new BigDecimal("100"))
                .validFrom(LocalDate.now())
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("workFamily is required");
    }

    @Test
    void rejects_missing_work_type() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .basePrice(new BigDecimal("100"))
                .validFrom(LocalDate.now())
                .build()
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("workType is required");
    }

    @Test
    void rejects_missing_valid_from() {

        assertThatThrownBy(() ->
            CreateWorkTypePriceRuleCommand.builder()
                .workFamily("FIXED_PROSTHESIS")
                .workType("CROWN")
                .basePrice(new BigDecimal("100"))
                .build()
        )
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("validFrom is required");
    }
}
