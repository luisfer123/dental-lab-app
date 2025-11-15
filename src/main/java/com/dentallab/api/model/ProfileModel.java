package com.dentallab.api.model;

import java.time.OffsetDateTime;

import org.springframework.hateoas.RepresentationModel;

import com.dentallab.api.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class ProfileModel extends RepresentationModel<ProfileModel> {
    private Long id;
    private Long clientId;          // ✅ Added field
    private ProfileType type;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    protected ProfileModel() {}

    protected ProfileModel(Long id, Long clientId, ProfileType type,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Overloaded constructor used by some profile models that don't include clientId
    protected ProfileModel(Long id, ProfileType type,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this(id, null, type, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public ProfileType getType() { return type; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public void setType(ProfileType type) { this.type = type; }
    public void setCreatedAt(OffsetDateTime localDateTime) { this.createdAt = localDateTime; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}