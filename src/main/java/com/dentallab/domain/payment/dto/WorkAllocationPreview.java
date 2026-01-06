package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Allocation preview for a single work.
 */
public class WorkAllocationPreview {

    private Long workId;
    private String workLabel;

    private BigDecimal workPrice;
    private BigDecimal alreadyPaidAmount;
    private BigDecimal unpaidAmount;

    private BigDecimal maxAllocatableAmount;
    private BigDecimal allocatedAmount;

    public WorkAllocationPreview() {
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

    public BigDecimal getWorkPrice() {
        return workPrice;
    }

    public void setWorkPrice(BigDecimal workPrice) {
        this.workPrice = workPrice;
    }

    public BigDecimal getAlreadyPaidAmount() {
        return alreadyPaidAmount;
    }

    public void setAlreadyPaidAmount(BigDecimal alreadyPaidAmount) {
        this.alreadyPaidAmount = alreadyPaidAmount;
    }

    public BigDecimal getUnpaidAmount() {
        return unpaidAmount;
    }

    public void setUnpaidAmount(BigDecimal unpaidAmount) {
        this.unpaidAmount = unpaidAmount;
    }

    public BigDecimal getMaxAllocatableAmount() {
        return maxAllocatableAmount;
    }

    public void setMaxAllocatableAmount(BigDecimal maxAllocatableAmount) {
        this.maxAllocatableAmount = maxAllocatableAmount;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WorkAllocationPreview)) return false;
        WorkAllocationPreview that = (WorkAllocationPreview) o;
        return Objects.equals(workId, that.workId)
                && Objects.equals(allocatedAmount, that.allocatedAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, allocatedAmount);
    }

    @Override
    public String toString() {
        return "WorkAllocationPreview{" +
                "workId=" + workId +
                ", allocatedAmount=" + allocatedAmount +
                '}';
    }
}
