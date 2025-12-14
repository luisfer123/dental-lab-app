package com.dentallab.service;

import java.util.List;

import com.dentallab.api.model.PaymentCreateRequest;
import com.dentallab.api.model.PaymentModel;

public interface PaymentService {

    PaymentModel registerPayment(PaymentCreateRequest request);
    
    PaymentModel getPaymentById(Long paymentId);

    List<PaymentModel> getPaymentsByClient(Long clientId);
}
