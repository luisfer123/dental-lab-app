package com.dentallab.api.model;

import java.util.Objects;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;

/**
 * API model representing a crown-type dental work.
 * Mirrors the structure of the crown_work table and extends WorkExtensionModel.
 */
public class CrownWorkModel extends WorkExtensionModel {

    private String toothNumber;                // e.g. "16", "24"
    private FixProstheticConstitution constitution;         // MONOLITHIC / STRATIFIED / METAL / TEMPORARY
    private BuildingTechnique buildingTechnique; // DIGITAL / MANUAL / HYBRID
    private Long coreMaterialId;               // material_id (core or single material)
    private Long veneeringMaterialId;          // optional if stratified or metal
    private Boolean isMonolithic;              // derived or computed for convenience

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public String getToothNumber() {
        return toothNumber;
    }

    public void setToothNumber(String toothNumber) {
        this.toothNumber = toothNumber;
    }

    public FixProstheticConstitution getConstitution() {
        return constitution;
    }

    public void setConstitution(FixProstheticConstitution constitution) {
        this.constitution = constitution;
    }

    public BuildingTechnique getBuildingTechnique() {
        return buildingTechnique;
    }

    public void setBuildingTechnique(BuildingTechnique buildingTechnique) {
        this.buildingTechnique = buildingTechnique;
    }

    public Long getCoreMaterialId() {
        return coreMaterialId;
    }

    public void setCoreMaterialId(Long coreMaterialId) {
        this.coreMaterialId = coreMaterialId;
    }

    public Long getVeneeringMaterialId() {
        return veneeringMaterialId;
    }

    public void setVeneeringMaterialId(Long veneeringMaterialId) {
        this.veneeringMaterialId = veneeringMaterialId;
    }

    public Boolean getIsMonolithic() {
        return isMonolithic;
    }

    public void setIsMonolithic(Boolean isMonolithic) {
        this.isMonolithic = isMonolithic;
    }

    // ==========================================================
    // EQUALITY & STRING REPRESENTATION
    // ==========================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrownWorkModel)) return false;
        if (!super.equals(o)) return false;
        CrownWorkModel that = (CrownWorkModel) o;
        return Objects.equals(toothNumber, that.toothNumber)
                && constitution == that.constitution
                && buildingTechnique == that.buildingTechnique
                && Objects.equals(coreMaterialId, that.coreMaterialId)
                && Objects.equals(veneeringMaterialId, that.veneeringMaterialId)
                && Objects.equals(isMonolithic, that.isMonolithic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), toothNumber, constitution,
                buildingTechnique, coreMaterialId, veneeringMaterialId, isMonolithic);
    }

    @Override
    public String toString() {
        return "CrownWorkModel{" +
                "workId=" + getWorkId() +
                ", toothNumber='" + toothNumber + '\'' +
                ", constitution=" + constitution +
                ", buildingTechnique=" + buildingTechnique +
                ", coreMaterialId=" + coreMaterialId +
                ", veneeringMaterialId=" + veneeringMaterialId +
                ", isMonolithic=" + isMonolithic +
                ", notes='" + getNotes() + '\'' +
                '}';
    }
}
