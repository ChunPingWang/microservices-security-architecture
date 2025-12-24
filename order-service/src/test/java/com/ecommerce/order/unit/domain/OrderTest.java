package com.ecommerce.order.unit.domain;

import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.entities.OrderItem;
import com.ecommerce.order.domain.value_objects.OrderStatus;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Order Aggregate Tests")
class OrderTest {

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Order Creation")
    class OrderCreation {

        @Test
        @DisplayName("should create order with items from cart")
        void shouldCreateOrderWithItemsFromCart() {
            List<OrderItem> items = List.of(
                    OrderItem.create(PRODUCT_ID, "Test Product", "SKU-001",
                            Money.of(new BigDecimal("999.00")), 2)
            );

            Order order = Order.createFromCart(CUSTOMER_ID, items, null);

            assertThat(order.getId()).isNotNull();
            assertThat(order.getCustomerId()).isEqualTo(CUSTOMER_ID);
            assertThat(order.getItems()).hasSize(1);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
            assertThat(order.getTotal().getAmount()).isEqualByComparingTo(new BigDecimal("1998.00"));
        }

        @Test
        @DisplayName("should create order with multiple items")
        void shouldCreateOrderWithMultipleItems() {
            List<OrderItem> items = List.of(
                    OrderItem.create(PRODUCT_ID, "Product A", "SKU-A",
                            Money.of(new BigDecimal("100.00")), 2),
                    OrderItem.create(UUID.randomUUID(), "Product B", "SKU-B",
                            Money.of(new BigDecimal("150.00")), 3)
            );

            Order order = Order.createFromCart(CUSTOMER_ID, items, null);

            assertThat(order.getItems()).hasSize(2);
            // 2*100 + 3*150 = 200 + 450 = 650
            assertThat(order.getTotal().getAmount()).isEqualByComparingTo(new BigDecimal("650.00"));
        }

        @Test
        @DisplayName("should reject order with no items")
        void shouldRejectOrderWithNoItems() {
            assertThatThrownBy(() -> Order.createFromCart(CUSTOMER_ID, List.of(), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("items");
        }

        @Test
        @DisplayName("should apply discount when coupon provided")
        void shouldApplyDiscountWhenCouponProvided() {
            List<OrderItem> items = List.of(
                    OrderItem.create(PRODUCT_ID, "Test Product", "SKU-001",
                            Money.of(new BigDecimal("1000.00")), 1)
            );

            Order order = Order.createFromCart(CUSTOMER_ID, items,
                    new Order.CouponDiscount("SAVE10", Money.of(new BigDecimal("100.00"))));

            assertThat(order.getTotal().getAmount()).isEqualByComparingTo(new BigDecimal("900.00"));
            assertThat(order.getDiscount().getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        }
    }

    @Nested
    @DisplayName("Order Payment")
    class OrderPayment {

        @Test
        @DisplayName("should transition to paid status")
        void shouldTransitionToPaidStatus() {
            Order order = createPendingOrder();
            UUID paymentId = UUID.randomUUID();

            order.markAsPaid(paymentId);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaymentId()).isEqualTo(paymentId);
        }

        @Test
        @DisplayName("should reject payment for non-pending order")
        void shouldRejectPaymentForNonPendingOrder() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());

            assertThatThrownBy(() -> order.markAsPaid(UUID.randomUUID()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot mark order as paid");
        }
    }

    @Nested
    @DisplayName("Order Cancellation")
    class OrderCancellation {

        @Test
        @DisplayName("should cancel pending order")
        void shouldCancelPendingOrder() {
            Order order = createPendingOrder();

            order.cancel("Customer requested cancellation");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancellationReason()).isEqualTo("Customer requested cancellation");
        }

        @Test
        @DisplayName("should cancel paid order before shipping")
        void shouldCancelPaidOrderBeforeShipping() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());

            order.cancel("Customer requested refund");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("should not cancel shipped order")
        void shouldNotCancelShippedOrder() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());
            order.markAsShipped("TRACK123");

            assertThatThrownBy(() -> order.cancel("Too late"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel");
        }
    }

    @Nested
    @DisplayName("Order Shipping")
    class OrderShipping {

        @Test
        @DisplayName("should mark paid order as shipped")
        void shouldMarkPaidOrderAsShipped() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());

            order.markAsShipped("TRACK123");

            assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
            assertThat(order.getTrackingNumber()).isEqualTo("TRACK123");
        }

        @Test
        @DisplayName("should not ship unpaid order")
        void shouldNotShipUnpaidOrder() {
            Order order = createPendingOrder();

            assertThatThrownBy(() -> order.markAsShipped("TRACK123"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot ship");
        }
    }

    @Nested
    @DisplayName("Order Delivery")
    class OrderDelivery {

        @Test
        @DisplayName("should mark shipped order as delivered")
        void shouldMarkShippedOrderAsDelivered() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());
            order.markAsShipped("TRACK123");

            order.markAsDelivered();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
            assertThat(order.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("should not deliver unshipped order")
        void shouldNotDeliverUnshippedOrder() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());

            assertThatThrownBy(() -> order.markAsDelivered())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot mark as delivered");
        }
    }

    @Nested
    @DisplayName("Payment Timeout")
    class PaymentTimeout {

        @Test
        @DisplayName("should expire order after timeout")
        void shouldExpireOrderAfterTimeout() {
            Order order = createPendingOrder();

            order.expirePayment();

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_EXPIRED);
        }

        @Test
        @DisplayName("should not expire paid order")
        void shouldNotExpirePaidOrder() {
            Order order = createPendingOrder();
            order.markAsPaid(UUID.randomUUID());

            assertThatThrownBy(() -> order.expirePayment())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot expire");
        }
    }

    private Order createPendingOrder() {
        List<OrderItem> items = List.of(
                OrderItem.create(PRODUCT_ID, "Test Product", "SKU-001",
                        Money.of(new BigDecimal("999.00")), 2)
        );
        return Order.createFromCart(CUSTOMER_ID, items, null);
    }
}
