package com.dentallab.persistence.entity;

import com.dentallab.api.enums.BridgeVariant;
import com.dentallab.api.enums.BuildingTechnique;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a bridge-type dental work.
 * Mirrors the structure of the bridge_work table.
 * Each BridgeWorkEntity shares its primary key with WorkEntity (1:1 mapping).
 */
@Entity
@Table(name = "bridge_work")
public class BridgeWorkEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==========================================================
    // PRIMARY KEY (1:1 relationship with WorkEntity)
    // ==========================================================

    @Id
    @Column(name = "work_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "work_id", foreignKey = @ForeignKey(name = "fk_bridge_work_work"))
    private WorkEntity work;

    // ==========================================================
    // CORE FIELDS
    // ==========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "constitution", nullable = false, length = 20)
    private BridgeVariant variant;  // e.g. MONOLITHIC, STRATIFIED, METAL, TEMPORARY

    @Enumerated(EnumType.STRING)
    @Column(name = "building_technique", nullable = false, length = 20)
    private BuildingTechnique buildingTechnique; // DIGITAL, MANUAL, HYBRID

    @Lob
    @Column(name = "abutment_teeth", columnDefinition = "JSON")
    private String abutmentTeeth; // stored as JSON array (["24","26"])

    @Lob
    @Column(name = "pontic_teeth", columnDefinition = "JSON")
    private String ponticTeeth;   // stored as JSON array (["25"])

    @Column(name = "core_material_id")
    private Long coreMaterialId;

    @Column(name = "veneering_material_id")
    private Long veneeringMaterialId;

    @Column(name = "connector_type", length = 100)
    private String connectorType;

    @Column(name = "pontic_design", length = 100)
    private String ponticDesign;

    @Column(name = "notes", length = 500)
    private String notes;

    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    public BridgeWorkEntity() {}

    public BridgeWorkEntity(Long id, WorkEntity work, BridgeVariant variant,
                            BuildingTechnique buildingTechnique, String abutmentTeeth,
                            String ponticTeeth, Long coreMaterialId, Long veneeringMaterialId,
                            String connectorType, String ponticDesign, String notes) {
        this.id = id;
        this.work = work;
        this.variant = variant;
        this.buildingTechnique = buildingTechnique;
        this.abutmentTeeth = abutmentTeeth;
        this.ponticTeeth = ponticTeeth;
        this.coreMaterialId = coreMaterialId;
        this.veneeringMaterialId = veneeringMaterialId;
        this.connectorType = connectorType;
        this.ponticDesign = ponticDesign;
        this.notes = notes;
    }

    // ==========================================================
    // GETTERS & SETTERS
    // ==========================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public WorkEntity getWork() {
        return work;
    }

    public void setWork(WorkEntity work) {
        this.work = work;
    }

    public BridgeVariant getVariant() {
        return variant;
    }

    public void setVariant(BridgeVariant variant) {
        this.variant = variant;
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
    // EQUALITY & STRING REPRESENTATION
    // ==========================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BridgeWorkEntity)) return false;
        BridgeWorkEntity that = (BridgeWorkEntity) o;
        return Objects.equals(id, that.id)
                && variant == that.variant
                && buildingTechnique == that.buildingTechnique
                && Objects.equals(coreMaterialId, that.coreMaterialId)
                && Objects.equals(veneeringMaterialId, that.veneeringMaterialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, variant, buildingTechnique, coreMaterialId, veneeringMaterialId);
    }

    @Override
    public String toString() {
        return "BridgeWorkEntity{" +
                "id=" + id +
                ", variant=" + variant +
                ", buildingTechnique=" + buildingTechnique +
                ", abutmentTeeth=" + abutmentTeeth +
                ", ponticTeeth=" + ponticTeeth +
                ", connectorType='" + connectorType + '\'' +
                ", ponticDesign='" + ponticDesign + '\'' +
                '}';
    }
}
