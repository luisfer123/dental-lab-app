package com.dentallab.domain.pricing.exception;

public class DuplicateWorkTypePriceRuleException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateWorkTypePriceRuleException(String message) {
        super(message);
    }
	
	public DuplicateWorkTypePriceRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
