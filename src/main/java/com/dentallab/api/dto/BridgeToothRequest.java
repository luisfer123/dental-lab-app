package com.dentallab.api.dto;

public class BridgeToothRequest {

    private Long toothId;
    private String role;         // "ABUTMENT" or "PONTIC"
    private Integer positionIndex;

    public BridgeToothRequest() {}

    public BridgeToothRequest(Long toothId, String role, Integer positionIndex) {
        this.toothId = toothId;
        this.role = role;
        this.positionIndex = positionIndex;
    }

    public Long getToothId() { return toothId; }
    public void setToothId(Long toothId) { this.toothId = toothId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Integer getPositionIndex() { return positionIndex; }
    public void setPositionIndex(Integer positionIndex) { this.positionIndex = positionIndex; }
}
