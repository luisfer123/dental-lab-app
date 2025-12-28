package com.dentallab.persistence.entity;

import java.util.Objects;

import com.dentallab.api.enums.BuildingTechnique;
import com.dentallab.domain.enums.FixProstheticConstitution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Entity representing a crown-type prosthetic work.
 * Extends base WorkEntity (1:1 relation via shared primary key).
 */
@Entity
@Table(name = "crown_work")
public class CrownWorkEntity {

    @Id
    @Column(name = "work_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "work_id", foreignKey = @ForeignKey(name = "fk_crown_work_work"))
    private WorkEntity work;

    @Enumerated(EnumType.STRING)
    @Column(name = "constitution", nullable = false, length = 20)
    private FixProstheticConstitution constitution;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_technique", nullable = false, length = 20)
    private BuildingTechnique buildingTechnique;

    @Column(name = "tooth_number", nullable = false, length = 10)
    private String toothNumber;

    @Column(name = "core_material_id")
    private Long coreMaterialId;

    @Column(name = "veneering_material_id")
    private Long veneeringMaterialId;

    @Column(name = "notes", length = 500)
    private String notes;

    @Transient
    private Boolean isMonolithic; // derived flag, not stored

    // --------------------------------------------------
    // Constructors
    // --------------------------------------------------
    public CrownWorkEntity() {
    }

    public CrownWorkEntity(Long id, WorkEntity work, FixProstheticConstitution constitution, BuildingTechnique buildingTechnique,
                           String toothNumber, Long coreMaterialId, Long veneeringMaterialId, String notes) {
        this.id = id;
        this.work = work;
        this.constitution = constitution;
        this.buildingTechnique = buildingTechnique;
        this.toothNumber = toothNumber;
        this.coreMaterialId = coreMaterialId;
        this.veneeringMaterialId = veneeringMaterialId;
        this.notes = notes;
    }

    // --------------------------------------------------
    // Getters & Setters
    // --------------------------------------------------

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

    public String getToothNumber() {
        return toothNumber;
    }

    public void setToothNumber(String toothNumber) {
        this.toothNumber = toothNumber;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsMonolithic() {
        if (constitution != null) {
            return constitution == FixProstheticConstitution.MONOLITHIC;
        }
        return null;
    }

    public void setIsMonolithic(Boolean isMonolithic) {
        this.isMonolithic = isMonolithic;
    }

    // --------------------------------------------------
    // Equality & String representation
    // --------------------------------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CrownWorkEntity)) return false;
        CrownWorkEntity that = (CrownWorkEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(toothNumber, that.toothNumber) &&
                constitution == that.constitution &&
                buildingTechnique == that.buildingTechnique &&
                Objects.equals(coreMaterialId, that.coreMaterialId) &&
                Objects.equals(veneeringMaterialId, that.veneeringMaterialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, toothNumber, constitution, buildingTechnique, coreMaterialId, veneeringMaterialId);
    }

    @Override
    public String toString() {
        return "CrownWorkEntity{" +
                "id=" + id +
                ", constitution=" + constitution +
                ", buildingTechnique=" + buildingTechnique +
                ", toothNumber='" + toothNumber + '\'' +
                ", coreMaterialId=" + coreMaterialId +
                ", veneeringMaterialId=" + veneeringMaterialId +
                ", notes='" + notes + '\'' +
                '}';
    }
}
