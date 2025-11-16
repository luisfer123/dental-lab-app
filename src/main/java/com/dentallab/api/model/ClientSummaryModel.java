package com.dentallab.api.model;

import java.time.OffsetDateTime;

public class ClientSummaryModel {
	
	private Long id;
    private String displayName;
    private String firstName;
    private String secondName;
    private String lastName;
    private String secondLastName;
    private String primaryEmail;
    private String primaryPhone;
    private String primaryAddress;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    /* Getters & Setters (single-line) */
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getDisplayName() { return displayName; } public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getFirstName() { return firstName; } public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; } public void setSecondName(String secondName) { this.secondName = secondName; }
    public String getLastName() { return lastName; } public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSecondLastName() { return secondLastName; } public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }
    public String getPrimaryEmail() { return primaryEmail; } public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
    public String getPrimaryPhone() { return primaryPhone; } public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }
    public String getPrimaryAddress() { return primaryAddress; } public void setPrimaryAddress(String primaryAddress) { this.primaryAddress = primaryAddress; }
    public Boolean getActive() { return active; } public void setActive(Boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

}
