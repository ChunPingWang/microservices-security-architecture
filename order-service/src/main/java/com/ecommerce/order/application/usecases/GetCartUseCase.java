package com.ecommerce.order.application.usecases;

import com.ecommerce.order.application.dto.CartResponse;
import com.ecommerce.order.domain.ports.CartRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Use case for retrieving shopping cart.
 */
@Service
public class GetCartUseCase {

    private final CartRepository cartRepository;

    public GetCartUseCase(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartResponse execute(UUID customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(CartResponse::from)
                .orElse(CartResponse.empty(customerId));
    }
}
