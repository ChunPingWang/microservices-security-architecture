package com.ecommerce.product.application.exceptions;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }

    public static ProductNotFoundException byId(String id) {
        return new ProductNotFoundException("Product not found with id: " + id);
    }

    public static ProductNotFoundException bySku(String sku) {
        return new ProductNotFoundException("Product not found with SKU: " + sku);
    }
}
