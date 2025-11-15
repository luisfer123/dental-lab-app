package com.dentallab.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BridgeVariant {
    MONOLITHIC("Monolithic"),
    STRATIFIED("Stratified"),
    METAL("Metal Framework");

    private final String label;

    BridgeVariant(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static BridgeVariant fromString(String value) {
        if (value == null) return null;
        for (BridgeVariant v : BridgeVariant.values()) {
            if (v.name().equalsIgnoreCase(value) || v.label.equalsIgnoreCase(value)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown bridge variant: " + value);
    }
}
