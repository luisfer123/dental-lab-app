package com.dentallab.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;

public class PagingUtils {
	
	private static final Logger log = LoggerFactory.getLogger(PagingUtils.class);
	
	/** Helper to parse sort parameters */
	public static Sort parseSort(String[] sortParams) {
		log.debug("parseSort received: {}", Arrays.toString(sortParams));
	    if (sortParams == null || sortParams.length == 0) {
	        return Sort.unsorted();
	    }

	    List<Sort.Order> orders = new ArrayList<>();

	    for (String param : sortParams) {
	        if (param == null || param.isBlank()) continue;

	        String[] parts = param.split(",");
	        String property = parts[0].trim();
	        Sort.Direction direction = Sort.Direction.ASC; // default

	        if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
	            direction = Sort.Direction.DESC;
	        }

	        orders.add(new Sort.Order(direction, property));
	    }

	    return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
	}
	
	/**
     * Parses sort parameter into array format expected by @{link parseSort}.
     * Supports multiple sort criteria separated by semicolons.
     * 
     * Examples:
     *   - "createdAt,desc" → ["createdAt,desc"]
     *   - "status,asc;createdAt,desc" → ["status,asc", "createdAt,desc"]
     * 
     * @param sort Sort parameter string
     * @return Array of sort parameters for PagingUtils.parseSort()
     */
    public static String[] parseSortParameter(String sort) {
        if (sort == null || sort.isBlank()) {
            return new String[]{"createdAt,desc"};
        }
        
        // Support multiple sort criteria separated by semicolon
        // This allows URLs like: ?sort=status,asc;createdAt,desc
        if (sort.contains(";")) {
            return sort.split(";");
        }
        
        // Single sort criterion
        return new String[]{sort};
    }

}
