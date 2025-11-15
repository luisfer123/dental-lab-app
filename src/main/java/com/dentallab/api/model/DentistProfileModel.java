package com.dentallab.api.model;

import java.time.OffsetDateTime;

import com.dentallab.api.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DentistProfileModel extends ProfileModel {
    private String clinicName;

    public DentistProfileModel() { super(); }
    public DentistProfileModel(Long id, String clinicName, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        super(id, ProfileType.DENTIST, createdAt, updatedAt);
        this.clinicName = clinicName;
    }

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
}
