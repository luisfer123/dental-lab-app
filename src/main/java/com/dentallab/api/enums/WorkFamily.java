package com.dentallab.api.enums;

/**
 * Defines the high-level work family types.
 * Each corresponds to a specific extension table (CROWN, BRIDGE, etc.).
 */
public enum WorkFamily {
    CROWN,
    BRIDGE,
    INLAY,
    ONLAY,
    VENEER,
    IMPLANT;

    /**
     * Returns a safe enum value from a string, case-insensitive.
     * Useful for parsing inputs or validating API parameters.
     */
    public static WorkFamily fromString(String value) {
        if (value == null || value.isBlank()) return null;
        for (WorkFamily wf : values()) {
            if (wf.name().equalsIgnoreCase(value.trim())) {
                return wf;
            }
        }
        throw new IllegalArgumentException("Unknown WorkFamily: " + value);
    }
}
