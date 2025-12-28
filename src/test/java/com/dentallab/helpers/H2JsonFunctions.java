package com.dentallab.helpers;

public final class H2JsonFunctions {

    private H2JsonFunctions() {}

    /**
     * Mimics MySQL JSON_LENGTH().
     * Works for JSON arrays stored as text.
     */
    public static int jsonLength(String json) {
        if (json == null || json.isBlank()) {
            return 0;
        }

        // very defensive: trim spaces
        json = json.trim();

        // Not an array → length = 1 (MySQL behavior)
        if (!json.startsWith("[") || !json.endsWith("]")) {
            return 1;
        }

        // Empty array
        if ("[]".equals(json)) {
            return 0;
        }

        // Count commas + 1
        int commas = 0;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }

            if (!inString && c == ',') {
                commas++;
            }
        }

        return commas + 1;
    }
}
