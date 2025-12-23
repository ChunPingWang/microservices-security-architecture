package com.ecommerce.product.application.exceptions;

/**
 * Exception thrown when a category is not found.
 */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public static CategoryNotFoundException byId(String id) {
        return new CategoryNotFoundException("Category not found with id: " + id);
    }
}
