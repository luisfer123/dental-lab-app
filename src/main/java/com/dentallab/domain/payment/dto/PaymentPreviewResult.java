package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * Immutable snapshot describing the <strong>proposed outcome</strong>
 * of registering a payment, prior to persistence.
 * </p>
 *
 * <p>
 * This class represents the authoritative result of the
 * <em>payment preview phase</em> and is intended to be reviewed
 * by a technician before any financial data is committed.
 * </p>
 *
 * <h3>Conceptual role</h3>
 *
 * <p>
 * A {@code PaymentPreviewResult} answers the question:
 * </p>
 *
 * <blockquote>
 *   "If this payment were registered right now, exactly what would happen?"
 * </blockquote>
 *
 * <p>
 * It exposes, in explicit and human-readable form:
 * </p>
 *
 * <ul>
 *   <li>how much would be allocated to each selected work</li>
 *   <li>how much of the payment would remain unallocated</li>
 *   <li>whether additional confirmation is required</li>
 *   <li>which other unpaid works could receive remaining funds</li>
 * </ul>
 *
 * <h3>Important characteristics</h3>
 *
 * <ul>
 *   <li>
 *     This object is <strong>purely informational</strong> and has no side effects.
 *   </li>
 *   <li>
 *     It may be safely recomputed multiple times without altering system state.
 *   </li>
 *   <li>
 *     It contains no hidden or implied behavior; all consequences are made explicit.
 *   </li>
 * </ul>
 *
 * <h3>Balance confirmation gate</h3>
 *
 * <p>
 * If {@code requiresBalanceConfirmation} is {@code true}, the backend
 * must refuse to register the payment unless the technician explicitly
 * confirms that the remaining amount should be converted into client balance.
 * </p>
 *
 * <p>
 * This mechanism prevents accidental creation of client credit and
 * enforces deliberate financial intent.
 * </p>
 *
 * <h3>Design intent</h3>
 *
 * <p>
 * This class exists to eliminate ambiguity from payment registration.
 * </p>
 *
 * <p>
 * Any state that is not visible in {@code PaymentPreviewResult}
 * must not be persisted during the commit phase.
 * </p>
 */
public class PaymentPreviewResult {

    private Long clientId;
    private BigDecimal paymentAmount;

    private List<WorkAllocationPreview> workAllocations;

    private BigDecimal totalUnpaidSelected;
    private BigDecimal totalAllocated;
    private BigDecimal remainingUnallocated;

    private List<WorkSuggestion> suggestedAdditionalWorks;

    private boolean requiresBalanceConfirmation;

    private List<String> warnings;

    public PaymentPreviewResult() {
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public List<WorkAllocationPreview> getWorkAllocations() {
        return workAllocations;
    }

    public void setWorkAllocations(List<WorkAllocationPreview> workAllocations) {
        this.workAllocations = workAllocations;
    }

    public BigDecimal getTotalUnpaidSelected() {
        return totalUnpaidSelected;
    }

    public void setTotalUnpaidSelected(BigDecimal totalUnpaidSelected) {
        this.totalUnpaidSelected = totalUnpaidSelected;
    }

    public BigDecimal getTotalAllocated() {
        return totalAllocated;
    }

    public void setTotalAllocated(BigDecimal totalAllocated) {
        this.totalAllocated = totalAllocated;
    }

    public BigDecimal getRemainingUnallocated() {
        return remainingUnallocated;
    }

    public void setRemainingUnallocated(BigDecimal remainingUnallocated) {
        this.remainingUnallocated = remainingUnallocated;
    }

    public List<WorkSuggestion> getSuggestedAdditionalWorks() {
        return suggestedAdditionalWorks;
    }

    public void setSuggestedAdditionalWorks(List<WorkSuggestion> suggestedAdditionalWorks) {
        this.suggestedAdditionalWorks = suggestedAdditionalWorks;
    }

    public boolean isRequiresBalanceConfirmation() {
        return requiresBalanceConfirmation;
    }

    public void setRequiresBalanceConfirmation(boolean requiresBalanceConfirmation) {
        this.requiresBalanceConfirmation = requiresBalanceConfirmation;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentPreviewResult)) return false;
        PaymentPreviewResult that = (PaymentPreviewResult) o;
        return requiresBalanceConfirmation == that.requiresBalanceConfirmation
                && Objects.equals(clientId, that.clientId)
                && Objects.equals(paymentAmount, that.paymentAmount)
                && Objects.equals(workAllocations, that.workAllocations)
                && Objects.equals(totalUnpaidSelected, that.totalUnpaidSelected)
                && Objects.equals(totalAllocated, that.totalAllocated)
                && Objects.equals(remainingUnallocated, that.remainingUnallocated)
                && Objects.equals(suggestedAdditionalWorks, that.suggestedAdditionalWorks)
                && Objects.equals(warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clientId,
                paymentAmount,
                workAllocations,
                totalUnpaidSelected,
                totalAllocated,
                remainingUnallocated,
                suggestedAdditionalWorks,
                requiresBalanceConfirmation,
                warnings
        );
    }

    @Override
    public String toString() {
        return "PaymentPreviewResult{" +
                "clientId=" + clientId +
                ", paymentAmount=" + paymentAmount +
                ", workAllocations=" + workAllocations +
                ", totalUnpaidSelected=" + totalUnpaidSelected +
                ", totalAllocated=" + totalAllocated +
                ", remainingUnallocated=" + remainingUnallocated +
                ", suggestedAdditionalWorks=" + suggestedAdditionalWorks +
                ", requiresBalanceConfirmation=" + requiresBalanceConfirmation +
                ", warnings=" + warnings +
                '}';
    }
}
