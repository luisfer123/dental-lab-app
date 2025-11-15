package com.dentallab.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "work_step_template")
public class WorkStepTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "work_type", nullable = false, length = 50)
    private String workType;

    @Column(name = "step_code", nullable = false, length = 50)
    private String stepCode;

    @Column(name = "step_label", nullable = false, length = 100)
    private String stepLabel;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "is_digital", nullable = false)
    private Boolean digital = true;

    // ==========================
    // Getters / Setters
    // ==========================

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public String getStepCode() { return stepCode; }
    public void setStepCode(String stepCode) { this.stepCode = stepCode; }

    public String getStepLabel() { return stepLabel; }
    public void setStepLabel(String stepLabel) { this.stepLabel = stepLabel; }

    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }

    public Boolean getDigital() { return digital; }
    public void setDigital(Boolean digital) { this.digital = digital; }

    // ==========================
    // equals / hashCode
    // ==========================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkStepTemplateEntity)) return false;
        WorkStepTemplateEntity that = (WorkStepTemplateEntity) o;
        return Objects.equals(templateId, that.templateId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateId);
    }

    @Override
    public String toString() {
        return "WorkStepTemplateEntity{" +
                "templateId=" + templateId +
                ", workType='" + workType + '\'' +
                ", stepCode='" + stepCode + '\'' +
                '}';
    }
}
