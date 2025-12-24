package com.ecommerce.order.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign client for Product Service.
 */
@FeignClient(name = "product-service", url = "${product-service.url:http://localhost:8082}")
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getProduct(@PathVariable UUID productId);

    record ProductResponse(
            UUID id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            String currency,
            UUID categoryId,
            boolean active,
            StockInfo stockInfo
    ) {}

    record StockInfo(
            int available,
            boolean inStock,
            boolean lowStock
    ) {}
}
