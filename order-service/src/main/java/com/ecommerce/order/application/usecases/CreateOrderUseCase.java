package com.ecommerce.order.application.usecases;

import com.ecommerce.order.application.dto.CreateOrderCommand;
import com.ecommerce.order.application.dto.OrderResponse;
import com.ecommerce.order.application.exceptions.CartEmptyException;
import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.aggregates.Order;
import com.ecommerce.order.domain.entities.CartItem;
import com.ecommerce.order.domain.entities.OrderItem;
import com.ecommerce.order.domain.ports.CartRepository;
import com.ecommerce.order.domain.ports.OrderRepository;
import com.ecommerce.shared.domain.value_objects.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for creating an order from the customer's cart.
 */
@Service
public class CreateOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateOrderUseCase.class);

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;

    public CreateOrderUseCase(CartRepository cartRepository, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public OrderResponse execute(UUID customerId, CreateOrderCommand command) {
        log.info("Creating order for customer: {}", customerId);

        // Get cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartEmptyException("Cart is empty"));

        if (cart.isEmpty()) {
            throw new CartEmptyException("Cart is empty");
        }

        // Convert cart items to order items
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::toOrderItem)
                .toList();

        // Create coupon discount if provided
        Order.CouponDiscount couponDiscount = null;
        if (command.couponCode() != null && !command.couponCode().isBlank()) {
            // TODO: Validate coupon with sales-service
            // For now, we'll skip coupon validation
            log.info("Coupon code provided: {}", command.couponCode());
        }

        // Create order
        Order order = Order.createFromCart(customerId, orderItems, couponDiscount);

        // Save order
        order = orderRepository.save(order);
        log.info("Order created: {} with total: {}", order.getId(), order.getTotal());

        // Clear cart after successful order creation
        cart.clear();
        cartRepository.save(cart);

        return OrderResponse.from(order);
    }

    private OrderItem toOrderItem(CartItem cartItem) {
        return OrderItem.create(
                cartItem.getProductId(),
                cartItem.getProductName(),
                cartItem.getProductSku(),
                cartItem.getUnitPrice(),
                cartItem.getQuantity().getValue()
        );
    }
}
