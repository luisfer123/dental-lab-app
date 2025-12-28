package com.dentallab.persistence.entity;

import jakarta.persistence.*;

/**
 * Joint table for Many to Many relationship between {@linkplain ToothRefEntity} and
 * {@linkplain BridgeWorkEntity}
 */
@Entity
@Table(name = "bridge_tooth")
public class BridgeToothEntity {

    public enum Role {
        ABUTMENT,
        PONTIC
    }

    @EmbeddedId
    private BridgeToothId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("bridgeWorkId")
    @JoinColumn(name = "bridge_work_id", nullable = false)
    private BridgeWorkEntity bridgeWork;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("toothId")
    @JoinColumn(name = "tooth_id", nullable = false)
    private ToothRefEntity tooth;
    
    /**
     * ABUTMENT = supporting crowned tooth, PONTIC = suspended unit
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "position_index")
    private Integer positionIndex;

    /* ---------------- Constructors ---------------- */

    protected BridgeToothEntity() {
        // JPA only
    }

    public BridgeToothEntity(
            BridgeWorkEntity bridgeWork,
            ToothRefEntity tooth,
            Role role,
            Integer positionIndex
    ) {
        this.bridgeWork = bridgeWork;
        this.tooth = tooth;
        this.role = role;
        this.positionIndex = positionIndex;
        this.id = new BridgeToothId(
            bridgeWork.getWork().getId(),
            tooth.getId()
        );
    }

    /* ---------------- Getters ---------------- */

    public BridgeToothId getId() {
        return id;
    }

    public BridgeWorkEntity getBridgeWork() {
        return bridgeWork;
    }

    public ToothRefEntity getTooth() {
        return tooth;
    }

    public Role getRole() {
        return role;
    }

    public Integer getPositionIndex() {
        return positionIndex;
    }

    /* ---------------- Setters ---------------- */

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPositionIndex(Integer positionIndex) {
        this.positionIndex = positionIndex;
    }
}
