package com.ecommerce.order.application.dto;

import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.entities.OrderItem;
import com.ecommerce.order.domain.value_objects.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for order details.
 */
public record OrderResponse(
        UUID id,
        UUID customerId,
        List<OrderItemResponse> items,
        int itemCount,
        int totalQuantity,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        String currency,
        String couponCode,
        OrderStatus status,
        UUID paymentId,
        String trackingNumber,
        Instant createdAt,
        Instant paidAt,
        Instant shippedAt,
        Instant deliveredAt
) {
    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                items,
                order.getItemCount(),
                order.getTotalQuantity(),
                order.getSubtotal().getAmount(),
                order.getDiscount().getAmount(),
                order.getTotal().getAmount(),
                order.getTotal().getCurrency().getCurrencyCode(),
                order.getCouponCode(),
                order.getStatus(),
                order.getPaymentId(),
                order.getTrackingNumber(),
                order.getCreatedAt(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt()
        );
    }

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            String productName,
            String productSku,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal subtotal,
            String currency
    ) {
        public static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductSku(),
                    item.getUnitPrice().getAmount(),
                    item.getQuantity(),
                    item.getSubtotal().getAmount(),
                    item.getUnitPrice().getCurrency().getCurrencyCode()
            );
        }
    }
}
