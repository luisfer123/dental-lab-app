package com.dentallab.api.model;

import com.dentallab.api.enums.BridgeVariant;
import com.dentallab.api.enums.BuildingTechnique;
import java.util.Objects;

/**
 * API model representing a bridge-type dental work.
 * Mirrors the structure of the bridge_work table.
 */
public class BridgeWorkModel extends WorkExtensionModel {

    private BridgeVariant bridgeVariant;          // constitution ENUM: MONOLITHIC / STRATIFIED / METAL / TEMPORARY
    private BuildingTechnique buildingTechnique;  // fabrication method: DIGITAL / MANUAL / HYBRID
    private String abutmentTeeth;                 // JSON string (list of abutment tooth numbers)
    private String ponticTeeth;                   // JSON string (list of pontic tooth numbers)
    private Long coreMaterialId;                  // material_id (core or metal framework)
    private Long veneeringMaterialId;             // optional if stratified or metal
    private String connectorType;                 // e.g., "Double", "Round", "Modified ridge lap"
    private String ponticDesign;                  // e.g., "Ovate", "Modified ridge lap", "Sanitary"
    private String notes;                         // free text notes about design or adjustments

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public BridgeVariant getBridgeVariant() {
        return bridgeVariant;
    }

    public void setBridgeVariant(BridgeVariant bridgeVariant) {
        this.bridgeVariant = bridgeVariant;
    }

    public BuildingTechnique getBuildingTechnique() {
        return buildingTechnique;
    }

    public void setBuildingTechnique(BuildingTechnique buildingTechnique) {
        this.buildingTechnique = buildingTechnique;
    }

    public String getAbutmentTeeth() {
        return abutmentTeeth;
    }

    public void setAbutmentTeeth(String abutmentTeeth) {
        this.abutmentTeeth = abutmentTeeth;
    }

    public String getPonticTeeth() {
        return ponticTeeth;
    }

    public void setPonticTeeth(String ponticTeeth) {
        this.ponticTeeth = ponticTeeth;
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
        return bridgeVariant == that.bridgeVariant
                && buildingTechnique == that.buildingTechnique
                && Objects.equals(abutmentTeeth, that.abutmentTeeth)
                && Objects.equals(ponticTeeth, that.ponticTeeth)
                && Objects.equals(coreMaterialId, that.coreMaterialId)
                && Objects.equals(veneeringMaterialId, that.veneeringMaterialId)
                && Objects.equals(connectorType, that.connectorType)
                && Objects.equals(ponticDesign, that.ponticDesign)
                && Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), bridgeVariant, buildingTechnique, abutmentTeeth, ponticTeeth,
                coreMaterialId, veneeringMaterialId, connectorType, ponticDesign, notes);
    }

    // ==========================================================
    // TOSTRING
    // ==========================================================

    @Override
    public String toString() {
        return "BridgeWorkModel{" +
                "workId=" + getWorkId() +
                ", bridgeVariant=" + bridgeVariant +
                ", buildingTechnique=" + buildingTechnique +
                ", abutmentTeeth='" + abutmentTeeth + '\'' +
                ", ponticTeeth='" + ponticTeeth + '\'' +
                ", coreMaterialId=" + coreMaterialId +
                ", veneeringMaterialId=" + veneeringMaterialId +
                ", connectorType='" + connectorType + '\'' +
                ", ponticDesign='" + ponticDesign + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
