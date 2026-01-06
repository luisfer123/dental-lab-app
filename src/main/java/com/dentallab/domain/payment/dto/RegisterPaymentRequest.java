package com.dentallab.domain.payment.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * <p>
 * Command object representing the <strong>explicit confirmation</strong>
 * of a payment registration.
 * </p>
 *
 * <p>
 * This class is used exclusively in the <em>commit phase</em> of the payment flow
 * and represents a technician-approved decision to persist a real payment
 * and its associated allocations.
 * </p>
 *
 * <h3>Conceptual role</h3>
 *
 * <p>
 * A {@code RegisterPaymentRequest} captures the final, unambiguous intent of:
 * </p>
 *
 * <ul>
 *   <li>recording a real payment received by the lab</li>
 *   <li>allocating specific amounts of that payment to specific works</li>
 *   <li>optionally converting any remaining amount into client balance</li>
 * </ul>
 *
 * <p>
 * By the time this object is created, all calculations, suggestions,
 * and warnings must already have been resolved during the preview phase.
 * </p>
 *
 * <h3>Relationship with payment preview</h3>
 *
 * <p>
 * This request is typically constructed <strong>from a prior
 * {@code PaymentPreviewResult}</strong> after the technician has reviewed
 * and explicitly confirmed the proposed allocation plan.
 * </p>
 *
 * <p>
 * The backend must treat the contents of this request as authoritative,
 * subject only to invariant re-validation at commit time.
 * </p>
 *
 * <h3>Important usage rules</h3>
 *
 * <ul>
 *   <li>
 *     {@code paymentAmount} represents the total real-world money received
 *     and must match the amount recorded in the {@code payment} table.
 *   </li>
 *   <li>
 *     {@code allocations} contains the final allocation commands that will
 *     be persisted verbatim into {@code payment_allocation}.
 *   </li>
 *   <li>
 *     If the sum of allocation amounts is less than {@code paymentAmount},
 *     {@code moveRemainderToBalance} must be explicitly set to {@code true}
 *     in order to create a balance credit.
 *   </li>
 *   <li>
 *     The backend must never silently move remaining funds to balance
 *     without this explicit confirmation.
 *   </li>
 * </ul>
 *
 * <h3>Design intent</h3>
 *
 * <p>
 * This class deliberately separates:
 * </p>
 *
 * <ul>
 *   <li><strong>decision-making</strong> (preview phase)</li>
 *   <li><strong>confirmation</strong> (technician intent)</li>
 *   <li><strong>execution</strong> (transactional persistence)</li>
 * </ul>
 *
 * <p>
 * This separation ensures that payment registration is deterministic,
 * auditable, and free of implicit or inferred behavior.
 * </p>
 */
public class RegisterPaymentRequest {

    @NotNull
    private Long clientId;

    @NotNull
    @Positive
    private BigDecimal paymentAmount;

    private String method;
    private String reference;
    private String notes;

    @NotNull
    private List<PaymentAllocationCommand> allocations;

    private boolean moveRemainderToBalance;
    
    @NotNull
    @Size(min = 8, max = 64)
    private String idempotencyKey;

    public RegisterPaymentRequest() {
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PaymentAllocationCommand> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<PaymentAllocationCommand> allocations) {
        this.allocations = allocations;
    }

    public boolean isMoveRemainderToBalance() {
        return moveRemainderToBalance;
    }

    public void setMoveRemainderToBalance(boolean moveRemainderToBalance) {
        this.moveRemainderToBalance = moveRemainderToBalance;
    }
    
    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RegisterPaymentRequest)) return false;
        RegisterPaymentRequest that = (RegisterPaymentRequest) o;
        return moveRemainderToBalance == that.moveRemainderToBalance
                && Objects.equals(clientId, that.clientId)
                && Objects.equals(paymentAmount, that.paymentAmount)
                && Objects.equals(method, that.method)
                && Objects.equals(reference, that.reference)
                && Objects.equals(notes, that.notes)
                && Objects.equals(allocations, that.allocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clientId,
                paymentAmount,
                method,
                reference,
                notes,
                allocations,
                moveRemainderToBalance
        );
    }

    @Override
    public String toString() {
        return "RegisterPaymentRequest{" +
                "clientId=" + clientId +
                ", paymentAmount=" + paymentAmount +
                ", method='" + method + '\'' +
                ", reference='" + reference + '\'' +
                ", notes='" + notes + '\'' +
                ", allocations=" + allocations +
                ", moveRemainderToBalance=" + moveRemainderToBalance +
                '}';
    }
}
