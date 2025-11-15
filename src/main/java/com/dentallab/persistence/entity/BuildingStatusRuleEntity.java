package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "building_status_rule")
public class BuildingStatusRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long ruleId;

    @Column(name = "work_family", nullable = false, length = 50)
    private String workFamily;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "constitution", length = 50)
    private String constitution;

    @Column(name = "building_technique", length = 50)
    private String buildingTechnique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private BuildingStatusRefEntity status;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "is_terminal", nullable = false)
    private Boolean terminal;

    // ==========================
    // Getters / Setters
    // ==========================

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }

    public String getWorkFamily() { return workFamily; }
    public void setWorkFamily(String workFamily) { this.workFamily = workFamily; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getConstitution() { return constitution; }
    public void setConstitution(String constitution) { this.constitution = constitution; }

    public String getBuildingTechnique() { return buildingTechnique; }
    public void setBuildingTechnique(String buildingTechnique) { this.buildingTechnique = buildingTechnique; }

    public BuildingStatusRefEntity getStatus() { return status; }
    public void setStatus(BuildingStatusRefEntity status) { this.status = status; }

    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public Boolean getTerminal() { return terminal; }
    public void setTerminal(Boolean terminal) { this.terminal = terminal; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildingStatusRuleEntity)) return false;
        BuildingStatusRuleEntity that = (BuildingStatusRuleEntity) o;
        return Objects.equals(ruleId, that.ruleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId);
    }

    @Override
    public String toString() {
        return "BuildingStatusRuleEntity{" +
                "ruleId=" + ruleId +
                ", workFamily='" + workFamily + '\'' +
                ", type='" + type + '\'' +
                ", constitution='" + constitution + '\'' +
                ", buildingTechnique='" + buildingTechnique + '\'' +
                ", sequenceOrder=" + sequenceOrder +
                ", terminal=" + terminal +
                '}';
    }
}
