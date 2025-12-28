package com.dentallab.api.model;

import java.util.Objects;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;

/**
 * API model representing a bridge-type dental work.
 * Mirrors the structure of the bridge_work table.
 */
public class BridgeWorkModel extends WorkExtensionModel {

    private FixProstheticConstitution constitution;          // constitution ENUM: MONOLITHIC / STRATIFIED / METAL / TEMPORARY
    private BuildingTechnique buildingTechnique;  // fabrication method: DIGITAL / MANUAL / HYBRID
    private Long coreMaterialId;                  // material_id (core or metal framework)
    private Long veneeringMaterialId;             // optional if stratified or metal
    private String connectorType;                 // e.g., "Double", "Round", "Modified ridge lap"
    private String ponticDesign;                  // e.g., "Ovate", "Modified ridge lap", "Sanitary"
    private String notes;                         // free text notes about design or adjustments

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

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

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getPonticDesign() {
        return ponticDesign;
    }

    public void setPonticDesign(String ponticDesign) {
        this.ponticDesign = ponticDesign;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ==========================================================
    // EQUALITY & HASHCODE
    // ==========================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BridgeWorkModel)) return false;
        if (!super.equals(o)) return false;
        BridgeWorkModel that = (BridgeWorkModel) o;
        return constitution == that.constitution
                && buildingTechnique == that.buildingTechnique
                && Objects.equals(coreMaterialId, that.coreMaterialId)
                && Objects.equals(veneeringMaterialId, that.veneeringMaterialId)
                && Objects.equals(connectorType, that.connectorType)
                && Objects.equals(ponticDesign, that.ponticDesign)
                && Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), constitution, buildingTechnique,
                coreMaterialId, veneeringMaterialId, connectorType, ponticDesign, notes);
    }

    // ==========================================================
    // TOSTRING
    // ==========================================================

    @Override
    public String toString() {
        return "BridgeWorkModel{" +
                "workId=" + getWorkId() +
                ", constitution=" + constitution +
                ", buildingTechnique=" + buildingTechnique +
                ", coreMaterialId=" + coreMaterialId +
                ", veneeringMaterialId=" + veneeringMaterialId +
                ", connectorType='" + connectorType + '\'' +
                ", ponticDesign='" + ponticDesign + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
