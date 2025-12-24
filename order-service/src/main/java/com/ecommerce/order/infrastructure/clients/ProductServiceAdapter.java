package com.ecommerce.order.infrastructure.clients;

import com.ecommerce.order.domain.ports.ProductServicePort;
import com.ecommerce.shared.domain.value_objects.Money;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements ProductServicePort using Feign client.
 */
@Component
public class ProductServiceAdapter implements ProductServicePort {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceAdapter.class);

    private final ProductServiceClient productServiceClient;

    public ProductServiceAdapter(ProductServiceClient productServiceClient) {
        this.productServiceClient = productServiceClient;
    }

    @Override
    public Optional<ProductInfo> getProductInfo(UUID productId) {
        try {
            ProductServiceClient.ProductResponse response = productServiceClient.getProduct(productId);
            if (response == null) {
                return Optional.empty();
            }

            int availableStock = response.stockInfo() != null ? response.stockInfo().available() : 0;

            return Optional.of(new ProductInfo(
                    response.id(),
                    response.name(),
                    response.sku(),
                    Money.of(response.price()),
                    availableStock,
                    response.active()
            ));
        } catch (FeignException.NotFound e) {
            log.debug("Product not found: {}", productId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product information", e);
        }
    }

    @Override
    public boolean isStockAvailable(UUID productId, int quantity) {
        return getProductInfo(productId)
                .map(info -> info.availableStock() >= quantity)
                .orElse(false);
    }
}
