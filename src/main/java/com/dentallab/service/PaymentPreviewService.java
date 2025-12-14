package com.dentallab.service;

import com.dentallab.api.model.PaymentPreviewRequest;
import com.dentallab.api.model.PaymentPreviewResponse;

public interface PaymentPreviewService {

    PaymentPreviewResponse previewPayment(PaymentPreviewRequest request);
}
