package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "work_status_ref")
public class WorkStatusRefEntity {

    @Id
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "label")
    private String label;
    
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    // ==========================
    // Getters / Setters
    // ==========================

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    
    public Integer getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(Integer sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkStatusRefEntity)) return false;
        WorkStatusRefEntity that = (WorkStatusRefEntity) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "WorkStatusRefEntity{" +
                "code='" + code + '\'' +
                ", label='" + label + '\'' +
                ", sequenceOrder=" + sequenceOrder +
                '}';
    }
}
