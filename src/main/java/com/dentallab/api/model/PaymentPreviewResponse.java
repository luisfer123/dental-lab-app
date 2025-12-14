package com.dentallab.api.model;

import java.math.BigDecimal;
import java.util.List;

public class PaymentPreviewResponse {

    private BigDecimal paymentAmount;

    private BigDecimal allocatedToWorks;

    private BigDecimal allocatedToWallet;

    private BigDecimal unallocatedAmount;

    private boolean fullyAllocated;

    private List<WorkAllocationSuggestion> suggestedAllocations;

    private String message;

    public BigDecimal getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(BigDecimal paymentAmount) { this.paymentAmount = paymentAmount; }

    public BigDecimal getAllocatedToWorks() { return allocatedToWorks; }
    public void setAllocatedToWorks(BigDecimal allocatedToWorks) { this.allocatedToWorks = allocatedToWorks; }

    public BigDecimal getAllocatedToWallet() { return allocatedToWallet; }
    public void setAllocatedToWallet(BigDecimal allocatedToWallet) { this.allocatedToWallet = allocatedToWallet; }

    public BigDecimal getUnallocatedAmount() { return unallocatedAmount; }
    public void setUnallocatedAmount(BigDecimal unallocatedAmount) { this.unallocatedAmount = unallocatedAmount; }

    public boolean isFullyAllocated() { return fullyAllocated; }
    public void setFullyAllocated(boolean fullyAllocated) { this.fullyAllocated = fullyAllocated; }

    public List<WorkAllocationSuggestion> getSuggestedAllocations() { return suggestedAllocations; }
    public void setSuggestedAllocations(List<WorkAllocationSuggestion> suggestedAllocations) { this.suggestedAllocations = suggestedAllocations; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
