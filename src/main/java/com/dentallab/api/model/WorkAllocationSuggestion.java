package com.dentallab.api.model;

import java.math.BigDecimal;

public class WorkAllocationSuggestion {

    private Long workId;

    private BigDecimal remainingDue;

    private BigDecimal suggestedAmount;

    public Long getWorkId() { return workId; }
    public void setWorkId(Long workId) { this.workId = workId; }

    public BigDecimal getRemainingDue() { return remainingDue; }
    public void setRemainingDue(BigDecimal remainingDue) { this.remainingDue = remainingDue; }

    public BigDecimal getSuggestedAmount() { return suggestedAmount; }
    public void setSuggestedAmount(BigDecimal suggestedAmount) { this.suggestedAmount = suggestedAmount; }
}
