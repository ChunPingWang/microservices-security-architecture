package com.ecommerce.product.application.usecases;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.exceptions.ProductNotFoundException;
import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.domain.ports.ProductRepository;
import com.ecommerce.product.domain.value_objects.SKU;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case for getting product details.
 */
@Service
public class GetProductDetailUseCase {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public GetProductDetailUseCase(
            ProductRepository productRepository,
            InventoryRepository inventoryRepository
    ) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Gets product details by ID.
     */
    @Transactional(readOnly = true)
    public ProductResponse execute(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ProductNotFoundException.byId(productId.toString()));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(null);

        return ProductResponse.from(product, inventory);
    }

    /**
     * Gets product details by SKU.
     */
    @Transactional(readOnly = true)
    public ProductResponse executeBySku(String skuValue) {
        SKU sku = SKU.of(skuValue);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> ProductNotFoundException.bySku(skuValue));

        Inventory inventory = inventoryRepository.findByProductId(product.getId())
                .orElse(null);

        return ProductResponse.from(product, inventory);
    }
}
