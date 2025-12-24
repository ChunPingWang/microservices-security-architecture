package com.ecommerce.order.infrastructure.web.controllers;

import com.ecommerce.order.application.dto.AddToCartCommand;
import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.application.dto.UpdateCartItemCommand;
import com.ecommerce.order.application.usecases.AddToCartUseCase;
import com.ecommerce.order.application.usecases.GetCartUseCase;
import com.ecommerce.order.application.usecases.RemoveCartItemUseCase;
import com.ecommerce.order.application.usecases.UpdateCartItemUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

/**
 * REST controller for shopping cart operations.
 */
@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final AddToCartUseCase addToCartUseCase;
    private final UpdateCartItemUseCase updateCartItemUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final GetCartUseCase getCartUseCase;

    public CartController(
            AddToCartUseCase addToCartUseCase,
            UpdateCartItemUseCase updateCartItemUseCase,
            RemoveCartItemUseCase removeCartItemUseCase,
            GetCartUseCase getCartUseCase
    ) {
        this.addToCartUseCase = addToCartUseCase;
        this.updateCartItemUseCase = updateCartItemUseCase;
        this.removeCartItemUseCase = removeCartItemUseCase;
        this.getCartUseCase = getCartUseCase;
    }

    /**
     * Get the current customer's cart.
     */
    @GetMapping
    public ResponseEntity<CartResponse> getCart(Principal principal) {
        UUID customerId = UUID.fromString(principal.getName());
        CartResponse cart = getCartUseCase.execute(customerId);
        return ResponseEntity.ok(cart);
    }

    /**
     * Add an item to the cart.
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            Principal principal,
            @Valid @RequestBody AddToCartCommand command
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        CartResponse cart = addToCartUseCase.execute(customerId, command);
        return ResponseEntity.ok(cart);
    }

    /**
     * Update the quantity of an item in the cart.
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Principal principal,
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateCartItemCommand command
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        CartResponse cart = updateCartItemUseCase.execute(customerId, productId, command);
        return ResponseEntity.ok(cart);
    }

    /**
     * Remove an item from the cart.
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            Principal principal,
            @PathVariable UUID productId
    ) {
        UUID customerId = UUID.fromString(principal.getName());
        CartResponse cart = removeCartItemUseCase.execute(customerId, productId);
        return ResponseEntity.ok(cart);
    }
}
