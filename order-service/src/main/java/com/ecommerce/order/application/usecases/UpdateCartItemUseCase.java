package com.ecommerce.order.application.usecases;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.dto.UpdateCartItemCommand;
import com.ecommerce.order.application.exceptions.CartItemNotFoundException;
import com.ecommerce.order.application.exceptions.InsufficientStockException;
import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.ports.CartRepository;
import com.ecommerce.order.domain.ports.ProductServicePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for updating cart item quantity.
 */
@Service
public class UpdateCartItemUseCase {

    private final CartRepository cartRepository;
    private final ProductServicePort productService;

    public UpdateCartItemUseCase(CartRepository cartRepository, ProductServicePort productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public CartResponse execute(UUID customerId, UUID productId, UpdateCartItemCommand command) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartItemNotFoundException(productId));

        if (!cart.containsProduct(productId)) {
            throw new CartItemNotFoundException(productId);
        }

        // Validate stock availability for new quantity
        if (!productService.isStockAvailable(productId, command.quantity())) {
            throw new InsufficientStockException(productId, command.quantity());
        }

        cart.updateItemQuantity(productId, command.quantity());
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }
}
