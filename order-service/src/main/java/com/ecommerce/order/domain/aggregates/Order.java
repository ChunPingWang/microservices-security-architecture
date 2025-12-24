package com.ecommerce.order.domain.aggregates;

import com.ecommerce.order.domain.entities.OrderItem;
import com.ecommerce.order.domain.value_objects.OrderStatus;
import com.ecommerce.shared.domain.value_objects.Money;

import java.time.Instant;
import java.util.*;

/**
 * Order aggregate root.
 * Manages the lifecycle of an order from creation to delivery.
 */
public class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;
    private final Money subtotal;
    private final Money discount;
    private final Money total;
    private final String couponCode;
    private OrderStatus status;
    private UUID paymentId;
    private String trackingNumber;
    private String cancellationReason;
    private final Instant createdAt;
    private Instant paidAt;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Instant updatedAt;

    private Order(UUID id, UUID customerId, List<OrderItem> items,
                  Money subtotal, Money discount, Money total, String couponCode,
                  OrderStatus status, Instant createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.couponCode = couponCode;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * Creates a new order from cart items.
     */
    public static Order createFromCart(UUID customerId, List<OrderItem> items,
                                        CouponDiscount couponDiscount) {
        Objects.requireNonNull(customerId, "Customer ID must not be null");
        Objects.requireNonNull(items, "Items must not be null");

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one items");
        }

        Money subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(Money.zero(), Money::add);

        Money discount = couponDiscount != null ? couponDiscount.discount() : Money.zero();
        Money total = discount.getAmount().compareTo(subtotal.getAmount()) >= 0
                ? Money.zero()
                : subtotal.subtract(discount);

        String couponCode = couponDiscount != null ? couponDiscount.code() : null;

        return new Order(
                UUID.randomUUID(),
                customerId,
                items,
                subtotal,
                discount,
                total,
                couponCode,
                OrderStatus.PENDING_PAYMENT,
                Instant.now()
        );
    }

    /**
     * Reconstitutes an order from persistence.
     */
    public static Order reconstitute(UUID id, UUID customerId, List<OrderItem> items,
                                      Money subtotal, Money discount, Money total,
                                      String couponCode, OrderStatus status,
                                      UUID paymentId, String trackingNumber,
                                      String cancellationReason,
                                      Instant createdAt, Instant paidAt,
                                      Instant shippedAt, Instant deliveredAt,
                                      Instant updatedAt) {
        Order order = new Order(id, customerId, items, subtotal, discount, total,
                couponCode, status, createdAt);
        order.paymentId = paymentId;
        order.trackingNumber = trackingNumber;
        order.cancellationReason = cancellationReason;
        order.paidAt = paidAt;
        order.shippedAt = shippedAt;
        order.deliveredAt = deliveredAt;
        order.updatedAt = updatedAt;
        return order;
    }

    /**
     * Marks the order as paid.
     */
    public void markAsPaid(UUID paymentId) {
        if (!status.canPay()) {
            throw new IllegalStateException(
                    "Cannot mark order as paid. Current status: " + status);
        }
        Objects.requireNonNull(paymentId, "Payment ID must not be null");

        this.paymentId = paymentId;
        this.status = OrderStatus.PAID;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the order as shipped.
     */
    public void markAsShipped(String trackingNumber) {
        if (!status.canShip()) {
            throw new IllegalStateException(
                    "Cannot ship order. Current status: " + status);
        }
        Objects.requireNonNull(trackingNumber, "Tracking number must not be null");

        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
        this.shippedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the order as delivered.
     */
    public void markAsDelivered() {
        if (!status.canDeliver()) {
            throw new IllegalStateException(
                    "Cannot mark as delivered. Current status: " + status);
        }

        this.status = OrderStatus.DELIVERED;
        this.deliveredAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Cancels the order.
     */
    public void cancel(String reason) {
        if (!status.canCancel()) {
            throw new IllegalStateException(
                    "Cannot cancel order. Current status: " + status);
        }
        Objects.requireNonNull(reason, "Cancellation reason must not be null");

        this.cancellationReason = reason;
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    /**
     * Expires the payment window.
     */
    public void expirePayment() {
        if (!status.canExpire()) {
            throw new IllegalStateException(
                    "Cannot expire payment. Current status: " + status);
        }

        this.status = OrderStatus.PAYMENT_EXPIRED;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the order as refunded.
     */
    public void markAsRefunded() {
        if (status != OrderStatus.PAID && status != OrderStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot refund order. Current status: " + status);
        }

        this.status = OrderStatus.REFUNDED;
        this.updatedAt = Instant.now();
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getItemCount() {
        return items.size();
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public Money getSubtotal() {
        return subtotal;
    }

    public Money getDiscount() {
        return discount;
    }

    public Money getTotal() {
        return total;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id.equals(order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", status=" + status +
                ", total=" + total +
                ", itemCount=" + getItemCount() +
                '}';
    }

    /**
     * Coupon discount information.
     */
    public record CouponDiscount(String code, Money discount) {
        public CouponDiscount {
            Objects.requireNonNull(code, "Coupon code must not be null");
            Objects.requireNonNull(discount, "Discount must not be null");
        }
    }
}
