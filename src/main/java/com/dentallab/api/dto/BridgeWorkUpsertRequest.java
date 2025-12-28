package com.dentallab.api.dto;

import java.util.List;

public class BridgeWorkUpsertRequest {

    // keep your existing fields here (constitution, materials, etc.)
    private String constitution;
    private String buildingTechnique;
    private Long coreMaterialId;
    private Long veneeringMaterialId;
    private Long buildingStatusId;
    private String connectorType;
    private String ponticDesign;
    private String notes;

    // NEW: normalized teeth
    private List<BridgeToothRequest> teeth;

    public BridgeWorkUpsertRequest() {}

    public List<BridgeToothRequest> getTeeth() { return teeth; }
    public void setTeeth(List<BridgeToothRequest> teeth) { this.teeth = teeth; }

    // getters/setters for the rest...
    public String getConstitution() { return constitution; }
    public void setConstitution(String constitution) { this.constitution = constitution; }

    public String getBuildingTechnique() { return buildingTechnique; }
    public void setBuildingTechnique(String buildingTechnique) { this.buildingTechnique = buildingTechnique; }

    public Long getCoreMaterialId() { return coreMaterialId; }
    public void setCoreMaterialId(Long coreMaterialId) { this.coreMaterialId = coreMaterialId; }

    public Long getVeneeringMaterialId() { return veneeringMaterialId; }
    public void setVeneeringMaterialId(Long veneeringMaterialId) { this.veneeringMaterialId = veneeringMaterialId; }

    public Long getBuildingStatusId() { return buildingStatusId; }
    public void setBuildingStatusId(Long buildingStatusId) { this.buildingStatusId = buildingStatusId; }

    public String getConnectorType() { return connectorType; }
    public void setConnectorType(String connectorType) { this.connectorType = connectorType; }

    public String getPonticDesign() { return ponticDesign; }
    public void setPonticDesign(String ponticDesign) { this.ponticDesign = ponticDesign; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
