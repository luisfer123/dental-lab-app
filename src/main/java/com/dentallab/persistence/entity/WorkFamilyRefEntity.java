package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "work_family_ref")
public class WorkFamilyRefEntity {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "label", nullable = false)
    private String label;

    // ==========================
    // Getters / Setters
    // ==========================

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkFamilyRefEntity)) return false;
        WorkFamilyRefEntity that = (WorkFamilyRefEntity) o;
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
        return "WorkFamilyRefEntity{" +
                "code='" + code + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
