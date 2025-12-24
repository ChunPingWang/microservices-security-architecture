package com.ecommerce.order.application.usecases;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.exceptions.CartItemNotFoundException;
import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.ports.CartRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for removing items from shopping cart.
 */
@Service
public class RemoveCartItemUseCase {

    private final CartRepository cartRepository;

    public RemoveCartItemUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartResponse execute(UUID customerId, UUID productId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        if (!cart.containsProduct(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        cart.removeItem(productId);
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }
}
