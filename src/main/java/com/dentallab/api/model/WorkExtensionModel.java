package com.dentallab.api.model;

import org.springframework.hateoas.RepresentationModel;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Polymorphic superclass for work extension models.
 * Each subclass represents a specific work-family extension.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrownWorkModel.class, name = "CROWN"),
        @JsonSubTypes.Type(value = BridgeWorkModel.class, name = "BRIDGE")
})
public abstract class WorkExtensionModel extends RepresentationModel<WorkExtensionModel> {

    private Long workId;
    private String notes;
    private String type;

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
