package com.dentallab.domain.pricing.model;

public class BasePriceExistsResponse {
	
	private Boolean exists;
	
	public BasePriceExistsResponse(Boolean exists) {
		this.exists = exists;
	}
	
	public Boolean getExists() { return exists; }
	public void setExists(Boolean exists) {this.exists = exists; }

}
