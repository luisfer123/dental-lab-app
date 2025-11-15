package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "work_type_ref")
public class WorkTypeRefEntity {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_code", nullable = false)
    private WorkFamilyRefEntity family;

    // ==========================
    // Getters / Setters
    // ==========================

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public WorkFamilyRefEntity getFamily() { return family; }
    public void setFamily(WorkFamilyRefEntity family) { this.family = family; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkTypeRefEntity)) return false;
        WorkTypeRefEntity that = (WorkTypeRefEntity) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    // ==========================
    // toString
    // ==========================

    @Override
    public String toString() {
        return "WorkTypeRefEntity{" +
                "code='" + code + '\'' +
                ", label='" + label + '\'' +
                ", family=" + (family != null ? family.getCode() : null) +
                '}';
    }
}
