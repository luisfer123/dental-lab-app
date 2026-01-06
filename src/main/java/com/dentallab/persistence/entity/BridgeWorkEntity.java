package com.dentallab.persistence.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
    private FixProstheticConstitution constitution;  // e.g. MONOLITHIC, STRATIFIED, METAL, TEMPORARY

    @Enumerated(EnumType.STRING)
    @Column(name = "building_technique", nullable = false, length = 20)
    private BuildingTechnique buildingTechnique; // DIGITAL, MANUAL, HYBRID

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
    
    // ==============================================================
    // Many to One relationship with BridToothEntity which is a join
    // table for a Many to Many relationship with ToothRefEntity
    // ==============================================================
    @OneToMany(
	    mappedBy = "bridgeWork",
	    cascade = CascadeType.ALL,
	    orphanRemoval = true
	)
	private Set<BridgeToothEntity> bridgeTeeth = new HashSet<>();


    // ==========================================================
    // CONSTRUCTORS
    // ==========================================================

    public BridgeWorkEntity() { }

    public BridgeWorkEntity(WorkEntity work) {
        this.work = work;
    }

    public BridgeWorkEntity(
            WorkEntity work,
            FixProstheticConstitution constitution,
            BuildingTechnique buildingTechnique,
            Long coreMaterialId,
            Long veneeringMaterialId,
            String connectorType,
            String ponticDesign,
            String notes
    ) {
        this.work = work;
        this.id = work.getId();
        this.constitution = constitution;
        this.buildingTechnique = buildingTechnique;
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

    public WorkEntity getWork() {
        return work;
    }

    public void setWork(WorkEntity work) {
        this.work = work;
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
                && constitution == that.constitution
                && buildingTechnique == that.buildingTechnique
                && Objects.equals(coreMaterialId, that.coreMaterialId)
                && Objects.equals(veneeringMaterialId, that.veneeringMaterialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, constitution, buildingTechnique, coreMaterialId, veneeringMaterialId);
    }

    @Override
    public String toString() {
        return "BridgeWorkEntity{" +
                "id=" + id +
                ", constitution=" + constitution +
                ", buildingTechnique=" + buildingTechnique +
                ", connectorType='" + connectorType + '\'' +
                ", ponticDesign='" + ponticDesign + '\'' +
                '}';
    }

	public Set<BridgeToothEntity> getBridgeTeeth() {
		return bridgeTeeth;
	}

	public void setBridgeTeeth(Set<BridgeToothEntity> bridgeTeeth) {
		this.bridgeTeeth = bridgeTeeth;
	}
}
