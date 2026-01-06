package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * <p>
 * Command object representing a <strong>confirmed allocation of payment</strong>
 * to a specific work.
 * </p>
 *
 * <p>
 * This class is used <strong>exclusively</strong> during the
 * <em>payment registration (commit) phase</em> and must never be used
 * during payment preview or calculation.
 * </p>
 *
 * <h3>Conceptual role</h3>
 *
 * <p>
 * A {@code PaymentAllocationCommand} expresses an <strong>explicit and final intent</strong>
 * to apply a concrete monetary amount to a given work.
 * </p>
 *
 * <p>
 * It is the backend representation of the technician's confirmation that:
 * </p>
 *
 * <ul>
 *   <li>a payment is being registered</li>
 *   <li>a specific work will receive a specific amount</li>
 *   <li>the allocation has already been previewed and validated</li>
 * </ul>
 *
 * <p>
 * The backend must execute these commands <strong>exactly as provided</strong>,
 * after re-validating invariants, without recomputing or guessing allocation logic.
 * </p>
 *
 * <h3>Relationship with payment preview</h3>
 *
 * <p>
 * Instances of this class are typically derived from a prior
 * {@code PaymentPreviewResult}.
 * </p>
 *
 * <p>
 * During preview:
 * </p>
 *
 * <ul>
 *   <li>allocation amounts may be computed automatically (Option A)</li>
 *   <li>or proposed manually (Option B)</li>
 * </ul>
 *
 * <p>
 * Once confirmed by the technician, those amounts are materialized
 * as {@code PaymentAllocationCommand} instances for the commit phase.
 * </p>
 *
 * <h3>Important usage rules</h3>
 *
 * <ul>
 *   <li>
 *     {@code allocatedAmount} represents the <strong>exact amount</strong>
 *     to be persisted in {@code payment_allocation}.
 *   </li>
 *   <li>
 *     An allocation with {@code allocatedAmount = 0} is allowed and
 *     represents an explicit decision to not apply payment to that work.
 *   </li>
 *   <li>
 *     The backend must validate that {@code allocatedAmount} does not
 *     exceed the unpaid amount of the work <em>at commit time</em>.
 *   </li>
 *   <li>
 *     This class must never be used to infer pricing, unpaid amounts,
 *     or allocation capacity.
 *   </li>
 * </ul>
 *
 * <h3>Design intent</h3>
 *
 * <p>
 * This command object deliberately separates:
 * </p>
 *
 * <ul>
 *   <li><strong>calculation</strong> (preview phase)</li>
 *   <li><strong>confirmation</strong> (user decision)</li>
 *   <li><strong>execution</strong> (persistence)</li>
 * </ul>
 *
 * <p>
 * This separation ensures that the payment subsystem remains deterministic,
 * auditable, and resilient to future changes in allocation strategy.
 * </p>
 *
 * <p>
 * In other words: by the time this object is used, all ambiguity must
 * already have been resolved.
 * </p>
 */
public class PaymentAllocationCommand {

    @NotNull
    private Long workId;

    @NotNull
    @PositiveOrZero
    private BigDecimal allocatedAmount;

    public PaymentAllocationCommand() {
    }

    public Long getWorkId() {
        return workId;
    }

    public void setWorkId(Long workId) {
        this.workId = workId;
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
        if (!(o instanceof PaymentAllocationCommand)) return false;
        PaymentAllocationCommand that = (PaymentAllocationCommand) o;
        return Objects.equals(workId, that.workId)
                && Objects.equals(allocatedAmount, that.allocatedAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workId, allocatedAmount);
    }

    @Override
    public String toString() {
        return "PaymentAllocationCommand{" +
                "workId=" + workId +
                ", allocatedAmount=" + allocatedAmount +
                '}';
    }
}
