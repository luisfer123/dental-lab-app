package com.dentallab.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BridgeToothId implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Column(name = "bridge_work_id")
    private Long bridgeWorkId;

    @Column(name = "tooth_id")
    private Long toothId;

    /* ---------------- Constructors ---------------- */

    protected BridgeToothId() {
        // JPA only
    }

    public BridgeToothId(Long bridgeWorkId, Long toothId) {
        this.bridgeWorkId = bridgeWorkId;
        this.toothId = toothId;
    }

    /* ---------------- Getters ---------------- */

    public Long getBridgeWorkId() {
        return bridgeWorkId;
    }

    public Long getToothId() {
        return toothId;
    }

    /* ---------------- equals / hashCode ---------------- */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BridgeToothId)) return false;
        BridgeToothId that = (BridgeToothId) o;
        return Objects.equals(bridgeWorkId, that.bridgeWorkId)
            && Objects.equals(toothId, that.toothId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bridgeWorkId, toothId);
    }
}
