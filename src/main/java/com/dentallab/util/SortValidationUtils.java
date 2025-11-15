package com.dentallab.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for validating sort parameters against allowed fields.
 * Prevents sort injection attacks and invalid field references.
 * 
 * @author DentalLab Team
 * @version 1.0
 */
public class SortValidationUtils {

    private static final Logger log = LoggerFactory.getLogger(SortValidationUtils.class);

    // Allowed sort fields for WorkEntity
    private static final Set<String> WORK_SORTABLE_FIELDS = new HashSet<>(Arrays.asList(
        "id",
        "type",
        "workFamily",
        "description",
        "shade",
        "status",
        "createdAt",
        "updatedAt",
        "client.id",
        "client.name",
        "order.id",
        "order.orderNumber"
    ));

    private SortValidationUtils() {
        // Utility class - no instantiation
    }

    /**
     * Validates that all sort fields in the parameters are allowed.
     * 
     * @param sortParams Array of sort parameters (e.g., ["createdAt,desc", "status,asc"])
     * @param allowedFields Set of allowed field names
     * @return true if all fields are valid
     * @throws IllegalArgumentException if any field is not allowed
     */
    public static boolean validateSortFields(String[] sortParams, Set<String> allowedFields) {
        if (sortParams == null || sortParams.length == 0) {
            return true;
        }

        for (String param : sortParams) {
            if (param == null || param.isBlank()) {
                continue;
            }

            String[] parts = param.split(",");
            String field = parts[0].trim();

            if (!allowedFields.contains(field)) {
                log.warn("Attempted to sort by invalid field: {}", field);
                throw new IllegalArgumentException(
                    String.format("Invalid sort field: '%s'. Allowed fields: %s", 
                                field, allowedFields)
                );
            }
        }

        return true;
    }

    /**
     * Validates sort parameters for Work entities.
     * 
     * @param sortParams Array of sort parameters
     * @return true if all fields are valid
     * @throws IllegalArgumentException if any field is not allowed
     */
    public static boolean validateWorkSortFields(String[] sortParams) {
        return validateSortFields(sortParams, WORK_SORTABLE_FIELDS);
    }

    /**
     * Gets the set of allowed sortable fields for Work entities.
     * 
     * @return Set of allowed field names
     */
    public static Set<String> getWorkSortableFields() {
        return new HashSet<>(WORK_SORTABLE_FIELDS);
    }

    /**
     * Sanitizes sort parameters by removing invalid fields.
     * Instead of throwing an exception, this method filters out invalid fields.
     * 
     * @param sortParams Array of sort parameters
     * @param allowedFields Set of allowed field names
     * @return Sanitized array with only valid sort parameters
     */
    public static String[] sanitizeSortFields(String[] sortParams, Set<String> allowedFields) {
        if (sortParams == null || sortParams.length == 0) {
            return sortParams;
        }

        return Arrays.stream(sortParams)
            .filter(param -> {
                if (param == null || param.isBlank()) {
                    return false;
                }
                String field = param.split(",")[0].trim();
                boolean isValid = allowedFields.contains(field);
                
                if (!isValid) {
                    log.warn("Filtering out invalid sort field: {}", field);
                }
                
                return isValid;
            })
            .toArray(String[]::new);
    }
}