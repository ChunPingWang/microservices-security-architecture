package com.ecommerce.order.application.exceptions;

/**
 * Exception thrown when trying to checkout with an empty cart.
 */
public class CartEmptyException extends RuntimeException {

    public CartEmptyException(String message) {
        super(message);
    }
}
