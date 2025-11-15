package com.dentallab.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.RepresentationModel;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientFullModel extends RepresentationModel<ClientFullModel> {

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

    private final List<ProfileModel> profiles = new ArrayList<>();

    /* Getters & Setters */
    public Long getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getFirstName() { return firstName; }
    public String getSecondName() { return secondName; }
    public String getLastName() { return lastName; }
    public String getSecondLastName() { return secondLastName; }
    public String getPrimaryEmail() { return primaryEmail; }
    public String getPrimaryPhone() { return primaryPhone; }
    public String getPrimaryAddress() { return primaryAddress; }
    public Boolean getActive() { return active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<ProfileModel> getProfiles() { return profiles; }

    public void setId(Long id) { this.id = id; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setSecondLastName(String secondLastName) { this.secondLastName = secondLastName; }
    public void setPrimaryEmail(String primaryEmail) { this.primaryEmail = primaryEmail; }
    public void setPrimaryPhone(String primaryPhone) { this.primaryPhone = primaryPhone; }
    public void setPrimaryAddress(String primaryAddress) { this.primaryAddress = primaryAddress; }
    public void setActive(Boolean active) { this.active = active; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addProfile(ProfileModel profile) {
        if (profile != null) this.profiles.add(profile);
    }
    
    @Override
    public String toString() {
		return "ClientFullModel{" +
				"id=" + id +
				", displayName='" + displayName + '\'' +
				", firstName='" + firstName + '\'' +
				", secondName='" + secondName + '\'' +
				", lastName='" + lastName + '\'' +
				", secondLastName='" + secondLastName + '\'' +
				", primaryEmail='" + primaryEmail + '\'' +
				", primaryPhone='" + primaryPhone + '\'' +
				", primaryAddress='" + primaryAddress + '\'' +
				", active=" + active +
				", createdAt=" + createdAt +
				", updatedAt=" + updatedAt +
				", profiles=" + profiles +
				'}';
	}
}
