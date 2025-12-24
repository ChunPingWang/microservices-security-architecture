package com.ecommerce.payment.application.usecases;

import com.ecommerce.payment.application.dto.PaymentResponse;
import com.ecommerce.payment.application.dto.ProcessPaymentCommand;
import com.ecommerce.payment.application.exceptions.OrderNotFoundException;
import com.ecommerce.payment.application.exceptions.PaymentFailedException;
import com.ecommerce.payment.domain.aggregates.Payment;
import com.ecommerce.payment.domain.ports.OrderServicePort;
import com.ecommerce.payment.domain.ports.PaymentGatewayPort;
import com.ecommerce.payment.domain.ports.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for processing a payment.
 */
@Service
public class ProcessPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentUseCase.class);

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGateway;
    private final OrderServicePort orderService;

    public ProcessPaymentUseCase(PaymentRepository paymentRepository,
                                  PaymentGatewayPort paymentGateway,
                                  OrderServicePort orderService) {
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
        this.orderService = orderService;
    }

    @Transactional
    public PaymentResponse execute(UUID customerId, ProcessPaymentCommand command) {
        log.info("Processing payment for order: {}", command.orderId());

        // Get order info
        OrderServicePort.OrderInfo orderInfo = orderService.getOrderInfo(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        // Verify customer owns this order
        if (!orderInfo.customerId().equals(customerId)) {
            throw new OrderNotFoundException(command.orderId());
        }

        // Check for existing payment
        var existingPayment = paymentRepository.findByOrderId(command.orderId());
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            if (payment.getStatus().canComplete() || payment.getStatus() == com.ecommerce.payment.domain.value_objects.PaymentStatus.COMPLETED) {
                log.info("Payment already exists for order: {}", command.orderId());
                return PaymentResponse.from(payment);
            }
        }

        // Create payment
        Payment payment = Payment.create(
                command.orderId(),
                customerId,
                orderInfo.totalAmount(),
                command.paymentMethod()
        );

        payment = paymentRepository.save(payment);
        log.info("Payment created: {}", payment.getId());

        // Start processing
        payment.startProcessing();

        // Process through gateway
        ProcessPaymentCommand.PaymentDetails details = command.paymentDetails();
        PaymentGatewayPort.PaymentResult result = paymentGateway.processPayment(
                new PaymentGatewayPort.PaymentRequest(
                        payment.getId(),
                        payment.getAmount(),
                        payment.getPaymentMethod(),
                        details != null ? details.cardNumber() : null,
                        details != null ? details.cardHolderName() : null,
                        details != null ? details.expiryMonth() : null,
                        details != null ? details.expiryYear() : null,
                        details != null ? details.cvv() : null
                )
        );

        if (result.success()) {
            payment.complete(result.transactionId());
            log.info("Payment completed: {} with transaction: {}",
                    payment.getId(), result.transactionId());

            // Notify order service
            orderService.notifyPaymentComplete(command.orderId(), payment.getId());
        } else {
            payment.fail(result.errorMessage());
            log.warn("Payment failed: {} - {}", result.errorCode(), result.errorMessage());

            // Notify order service
            orderService.notifyPaymentFailed(command.orderId(), result.errorMessage());

            throw new PaymentFailedException(result.errorCode(), result.errorMessage());
        }

        payment = paymentRepository.save(payment);
        return PaymentResponse.from(payment);
    }
}
