package com.ecommerce.payment.unit.domain;

import com.ecommerce.payment.domain.aggregates.Payment;
import com.ecommerce.payment.domain.value_objects.PaymentMethod;
import com.ecommerce.payment.domain.value_objects.PaymentStatus;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment Aggregate Tests")
class PaymentTest {

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final UUID CUSTOMER_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Payment Creation")
    class PaymentCreation {

        @Test
        @DisplayName("should create pending payment")
        void shouldCreatePendingPayment() {
            Money amount = Money.of(new BigDecimal("999.00"));

            Payment payment = Payment.create(ORDER_ID, CUSTOMER_ID, amount, PaymentMethod.CREDIT_CARD);

            assertThat(payment.getId()).isNotNull();
            assertThat(payment.getOrderId()).isEqualTo(ORDER_ID);
            assertThat(payment.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
            assertThat(payment.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should reject negative amount")
        void shouldRejectNegativeAmount() {
            assertThatThrownBy(() -> Payment.create(ORDER_ID, CUSTOMER_ID,
                    Money.of(new BigDecimal("-100.00")), PaymentMethod.CREDIT_CARD))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject zero amount")
        void shouldRejectZeroAmount() {
            assertThatThrownBy(() -> Payment.create(ORDER_ID, CUSTOMER_ID,
                    Money.zero(), PaymentMethod.CREDIT_CARD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("should create payment with different payment methods")
        void shouldCreatePaymentWithDifferentPaymentMethods() {
            Money amount = Money.of(new BigDecimal("500.00"));

            Payment creditCard = Payment.create(ORDER_ID, CUSTOMER_ID, amount, PaymentMethod.CREDIT_CARD);
            Payment linePay = Payment.create(ORDER_ID, CUSTOMER_ID, amount, PaymentMethod.LINE_PAY);
            Payment applePay = Payment.create(ORDER_ID, CUSTOMER_ID, amount, PaymentMethod.APPLE_PAY);

            assertThat(creditCard.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
            assertThat(linePay.getPaymentMethod()).isEqualTo(PaymentMethod.LINE_PAY);
            assertThat(applePay.getPaymentMethod()).isEqualTo(PaymentMethod.APPLE_PAY);
        }
    }

    @Nested
    @DisplayName("Payment Processing")
    class PaymentProcessing {

        @Test
        @DisplayName("should mark payment as processing")
        void shouldMarkPaymentAsProcessing() {
            Payment payment = createPendingPayment();

            payment.startProcessing();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PROCESSING);
        }

        @Test
        @DisplayName("should complete payment successfully")
        void shouldCompletePaymentSuccessfully() {
            Payment payment = createPendingPayment();
            payment.startProcessing();
            String transactionId = "TXN-" + UUID.randomUUID();

            payment.complete(transactionId);

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(payment.getTransactionId()).isEqualTo(transactionId);
            assertThat(payment.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should fail payment")
        void shouldFailPayment() {
            Payment payment = createPendingPayment();
            payment.startProcessing();

            payment.fail("Insufficient funds");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureReason()).isEqualTo("Insufficient funds");
        }

        @Test
        @DisplayName("should not complete already completed payment")
        void shouldNotCompleteAlreadyCompletedPayment() {
            Payment payment = createPendingPayment();
            payment.startProcessing();
            payment.complete("TXN-123");

            assertThatThrownBy(() -> payment.complete("TXN-456"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot complete");
        }
    }

    @Nested
    @DisplayName("Payment Refund")
    class PaymentRefund {

        @Test
        @DisplayName("should refund completed payment fully")
        void shouldRefundCompletedPaymentFully() {
            Payment payment = createCompletedPayment();

            payment.refund(payment.getAmount(), "Customer requested refund");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            assertThat(payment.getRefundedAmount()).isEqualTo(payment.getAmount());
            assertThat(payment.getRefundReason()).isEqualTo("Customer requested refund");
        }

        @Test
        @DisplayName("should partially refund completed payment")
        void shouldPartiallyRefundCompletedPayment() {
            Payment payment = createCompletedPayment();
            Money refundAmount = Money.of(new BigDecimal("500.00"));

            payment.refund(refundAmount, "Partial refund");

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
            assertThat(payment.getRefundedAmount().getAmount())
                    .isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should not refund more than paid amount")
        void shouldNotRefundMoreThanPaidAmount() {
            Payment payment = createCompletedPayment();
            Money excessAmount = Money.of(new BigDecimal("2000.00"));

            assertThatThrownBy(() -> payment.refund(excessAmount, "Too much"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("exceed");
        }

        @Test
        @DisplayName("should not refund pending payment")
        void shouldNotRefundPendingPayment() {
            Payment payment = createPendingPayment();

            assertThatThrownBy(() -> payment.refund(payment.getAmount(), "No reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot refund");
        }
    }

    @Nested
    @DisplayName("Payment Expiration")
    class PaymentExpiration {

        @Test
        @DisplayName("should expire pending payment")
        void shouldExpirePendingPayment() {
            Payment payment = createPendingPayment();

            payment.expire();

            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        }

        @Test
        @DisplayName("should not expire completed payment")
        void shouldNotExpireCompletedPayment() {
            Payment payment = createCompletedPayment();

            assertThatThrownBy(() -> payment.expire())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot expire");
        }
    }

    private Payment createPendingPayment() {
        return Payment.create(ORDER_ID, CUSTOMER_ID,
                Money.of(new BigDecimal("999.00")), PaymentMethod.CREDIT_CARD);
    }

    private Payment createCompletedPayment() {
        Payment payment = createPendingPayment();
        payment.startProcessing();
        payment.complete("TXN-" + UUID.randomUUID());
        return payment;
    }
}
