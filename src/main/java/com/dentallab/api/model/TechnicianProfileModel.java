package com.dentallab.api.model;

import java.time.OffsetDateTime;

import com.dentallab.api.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TechnicianProfileModel extends ProfileModel {
    private String labName;
    private String specialization;
    private Boolean active;

    public TechnicianProfileModel() { super(); }
    public TechnicianProfileModel(Long id, String labName, String specialization, Boolean active,
                                  OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        super(id, ProfileType.TECHNICIAN, createdAt, updatedAt);
        this.labName = labName;
        this.specialization = specialization;
        this.active = active;
    }

    public String getLabName() { return labName; }
    public String getSpecialization() { return specialization; }
    public Boolean getActive() { return active; }
    public void setLabName(String labName) { this.labName = labName; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public void setActive(Boolean active) { this.active = active; }
}
