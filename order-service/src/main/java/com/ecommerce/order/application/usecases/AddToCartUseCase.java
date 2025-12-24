package com.ecommerce.order.application.usecases;

import com.ecommerce.order.application.dto.AddToCartCommand;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.exceptions.InsufficientStockException;
import com.ecommerce.order.application.exceptions.ProductNotFoundException;
import com.ecommerce.order.domain.aggregates.Cart;
import com.ecommerce.order.domain.ports.CartRepository;
import com.ecommerce.order.domain.ports.ProductServicePort;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for adding items to shopping cart.
 */
@Service
public class AddToCartUseCase {

    private final CartRepository cartRepository;
    private final ProductServicePort productService;

    public AddToCartUseCase(CartRepository cartRepository, ProductServicePort productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    public CartResponse execute(UUID customerId, AddToCartCommand command) {
        // Validate product exists and is active
        ProductServicePort.ProductInfo productInfo = productService.getProductInfo(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        if (!productInfo.active()) {
            throw new ProductNotFoundException(command.productId());
        }

        // Validate stock availability
        if (!productService.isStockAvailable(command.productId(), command.quantity())) {
            throw new InsufficientStockException(command.productId(), command.quantity());
        }

        // Get or create cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> Cart.create(customerId));

        // Check if adding to existing item would exceed stock
        int totalQuantity = command.quantity();
        if (cart.containsProduct(command.productId())) {
            totalQuantity += cart.getItem(command.productId()).get().getQuantityValue();
            if (!productService.isStockAvailable(command.productId(), totalQuantity)) {
                throw new InsufficientStockException(command.productId(), totalQuantity);
            }
        }

        // Add item to cart
        cart.addItem(
                productInfo.id(),
                productInfo.name(),
                productInfo.sku(),
                productInfo.price(),
                command.quantity()
        );

        // Save cart
        cartRepository.save(cart);

        return CartResponse.from(cart);
    }
}
