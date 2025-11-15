package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "building_status_ref")
public class BuildingStatusRefEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "description")
    private String description;

    // ==========================
    // Getters / Setters
    // ==========================

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuildingStatusRefEntity)) return false;
        BuildingStatusRefEntity that = (BuildingStatusRefEntity) o;
        return Objects.equals(statusId, that.statusId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusId);
    }

    @Override
    public String toString() {
        return "BuildingStatusRefEntity{" +
                "statusId=" + statusId +
                ", code='" + code + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
