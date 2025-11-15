package com.dentallab.api.model;

import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aggregated HATEOAS model representing a complete work,
 * including its base information and its specific extension details
 * (CrownWork, BridgeWork, etc.).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FullWorkModel extends RepresentationModel<FullWorkModel> {

    @JsonProperty("base")
    private WorkModel base;

    @JsonProperty("extension")
    private WorkExtensionModel extension;

    // --- Optional convenience fields ---
    @JsonProperty("family")
    private String workFamily;

    @JsonProperty("type")
    private String type;

    @JsonProperty("familyLabel")
    private String familyLabel;

    @JsonProperty("typeLabel")
    private String typeLabel;

    // ==========================================================
    // Constructors
    // ==========================================================

    public FullWorkModel() {}

    public FullWorkModel(WorkModel base, WorkExtensionModel extension) {
        this.base = base;
        this.extension = extension;
        if (base != null) {
            this.workFamily = base.getWorkFamily() != null ? base.getWorkFamily() : null;
            this.type = base.getType();
            this.familyLabel = base.getFamilyLabel();
            this.typeLabel = base.getTypeLabel();
        }
    }

    // ==========================================================
    // Getters & Setters
    // ==========================================================

    public WorkModel getBase() { return base; }
    public void setBase(WorkModel base) { 
        this.base = base;
        if (base != null) {
            this.workFamily = base.getWorkFamily() != null ? base.getWorkFamily() : null;
            this.type = base.getType();
            this.familyLabel = base.getFamilyLabel();
            this.typeLabel = base.getTypeLabel();
        }
    }

    public WorkExtensionModel getExtension() { return extension; }
    public void setExtension(WorkExtensionModel extension) { this.extension = extension; }

    public String getWorkFamily() { return workFamily; }
    public void setWorkFamily(String workFamily) { this.workFamily = workFamily; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getFamilyLabel() { return familyLabel; }
    public void setFamilyLabel(String familyLabel) { this.familyLabel = familyLabel; }

    public String getTypeLabel() { return typeLabel; }
    public void setTypeLabel(String typeLabel) { this.typeLabel = typeLabel; }
}
