package com.dentallab.domain.payment.service;

import java.math.BigDecimal;
import java.util.List;

import com.dentallab.domain.payment.model.PaymentAllocationRequest;
import com.dentallab.domain.payment.model.PaymentPreview;

public interface PaymentPreviewService {

    PaymentPreview preview(Long clientId,
                           BigDecimal paymentAmount,
                           List<PaymentAllocationRequest> requestedAllocations);
}
