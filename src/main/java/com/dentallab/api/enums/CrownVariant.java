package com.dentallab.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CrownVariant {
    MONOLITHIC("Monolithic"),
    STRATIFIED("Stratified"),
    PMMA_TEMPORARY("PMMA Temporary"),
    METAL("Metal with Ceramic");

    private final String label;

    CrownVariant(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static CrownVariant fromString(String value) {
        if (value == null) return null;
        for (CrownVariant variant : CrownVariant.values()) {
            if (variant.name().equalsIgnoreCase(value) || variant.getLabel().equalsIgnoreCase(value)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown crown variant: " + value);
    }
}
