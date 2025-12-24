package com.ecommerce.payment.infrastructure.web.controllers;

import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.application.exceptions.PaymentNotFoundException;
import com.ecommerce.payment.application.usecases.ProcessPaymentUseCase;
import com.ecommerce.payment.domain.ports.PaymentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final PaymentRepository paymentRepository;

    public PaymentController(ProcessPaymentUseCase processPaymentUseCase,
                             PaymentRepository paymentRepository) {
        this.processPaymentUseCase = processPaymentUseCase;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Process a payment for an order.
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(
            Principal principal,
            @Valid @RequestBody ProcessPaymentCommand command
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        PaymentResponse response = processPaymentUseCase.execute(customerId, command);
        return ResponseEntity.ok(response);
    }

    /**
     * Get payment by ID.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            Principal principal,
            @PathVariable UUID paymentId
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        // Verify customer owns this payment
        if (!payment.getCustomerId().equals(customerId)) {
            throw new PaymentNotFoundException(paymentId);
        }

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }

    /**
     * Get payments for the current customer.
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(Principal principal) {
        UUID customerId = UUID.fromString(principal.getName());
        List<PaymentResponse> payments = paymentRepository.findByCustomerId(customerId)
                .stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment by order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(
            Principal principal,
            @PathVariable UUID orderId
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        var payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException(orderId));

        // Verify customer owns this payment
        if (!payment.getCustomerId().equals(customerId)) {
            throw new PaymentNotFoundException(orderId);
        }

        return ResponseEntity.ok(PaymentResponse.from(payment));
    }
}
