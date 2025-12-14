package com.dentallab.api.controller;

import com.dentallab.api.model.PaymentCreateRequest;
import com.dentallab.api.model.PaymentModel;
import com.dentallab.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for handling client payments.
 * Controllers are thin: they delegate all logic to services
 * and only deal with API models.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ==========================================================
    // CREATE PAYMENT
    // ==========================================================

    /**
     * Registers a new payment and applies it to works.
     * Any excess amount is automatically added to client balance.
     */
    @PostMapping
    public ResponseEntity<PaymentModel> createPayment(
            @RequestBody PaymentCreateRequest request
    ) {
        PaymentModel payment = paymentService.registerPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    // ==========================================================
    // READ PAYMENT
    // ==========================================================

    /**
     * Retrieves a payment by its id.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentModel> getPaymentById(
            @PathVariable Long paymentId
    ) {
        PaymentModel payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Retrieves all payments for a given client.
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PaymentModel>> getPaymentsByClient(
            @PathVariable Long clientId
    ) {
        List<PaymentModel> payments = paymentService.getPaymentsByClient(clientId);
        return ResponseEntity.ok(payments);
    }
}
