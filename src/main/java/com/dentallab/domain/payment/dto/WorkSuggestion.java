package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Suggestion for an unpaid work that could receive remaining payment.
 */
public class WorkSuggestion {

    private Long workId;
    private String workLabel;
    private BigDecimal unpaidAmount;

    public WorkSuggestion() {
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
    }

    public String getWorkLabel() {
        return workLabel;
    }

    public void setWorkLabel(String workLabel) {
        this.workLabel = workLabel;
    }

    public BigDecimal getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkSuggestion)) return false;
        WorkSuggestion that = (WorkSuggestion) o;
        return Objects.equals(workId, that.workId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId);
    }

    @Override
    public String toString() {
        return "WorkSuggestion{" +
                "workId=" + workId +
                ", unpaidAmount=" + unpaidAmount +
                '}';
    }
}
