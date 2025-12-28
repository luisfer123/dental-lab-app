package com.dentallab.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tooth_ref")
public class ToothRefEntity {

    @Id
    @Column(name = "tooth_id")
    private Long id;

    @Column(
        name = "tooth_number",
        nullable = false,
        unique = true,
        length = 5
    )
    private String toothNumber;

    /* ---------------- Constructors ---------------- */

    public ToothRefEntity() { }

    public ToothRefEntity(Long id, String toothNumber) {
        this.id = id;
        this.toothNumber = toothNumber;
    }

    /* ---------------- Getters / Setters ---------------- */

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
    	this.id = id;
    }

    public String getToothNumber() {
        return toothNumber;
    }

    public void setToothNumber(String toothNumber) {
        this.toothNumber = toothNumber;
    }
}
