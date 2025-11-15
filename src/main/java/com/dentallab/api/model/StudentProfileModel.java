package com.dentallab.api.model;

import java.time.OffsetDateTime;

import com.dentallab.api.enums.ProfileType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentProfileModel extends ProfileModel {
    private String universityName;
    private Integer semester;

    public StudentProfileModel() { super(); }
    public StudentProfileModel(Long id, Long clientId, String universityName, Integer semester,
            OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        super(id, clientId, ProfileType.STUDENT, createdAt, updatedAt);
        this.universityName = universityName;
        this.semester = semester;
    }

    public String getUniversityName() { return universityName; }
    public Integer getSemester() { return semester; }
    public void setUniversityName(String universityName) { this.universityName = universityName; }
    public void setSemester(Integer semester) { this.semester = semester; }
}