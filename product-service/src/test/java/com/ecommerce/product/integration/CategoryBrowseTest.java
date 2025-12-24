package com.ecommerce.product.integration;

import com.ecommerce.product.domain.entities.Category;
import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.CategoryRepository;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.domain.ports.ProductRepository;
import com.ecommerce.product.domain.value_objects.SKU;
import com.ecommerce.shared.domain.value_objects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for category browsing functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Category Browse Integration Tests")
class CategoryBrowseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    private Category electronicsCategory;
    private Category clothingCategory;
    private Product laptop;
    private Product smartphone;

    @BeforeEach
    void setUp() {
        // Create categories
        electronicsCategory = Category.create("Electronics", "Electronic devices and gadgets", null);
        clothingCategory = Category.create("Clothing", "Fashion and apparel", null);
        categoryRepository.save(electronicsCategory);
        categoryRepository.save(clothingCategory);

        // Create products in electronics category
        laptop = Product.create(
                SKU.of("LAPTOP-001"),
                "Gaming Laptop",
                "High-end gaming laptop",
                Money.of(new BigDecimal("45999.00")),
                electronicsCategory.getId()
        );

        smartphone = Product.create(
                SKU.of("PHONE-001"),
                "Smartphone Pro",
                "Latest smartphone",
                Money.of(new BigDecimal("35999.00")),
                electronicsCategory.getId()
        );

        productRepository.save(laptop);
        productRepository.save(smartphone);

        // Create inventory
        inventoryRepository.save(Inventory.create(laptop.getId(), 50));
        inventoryRepository.save(Inventory.create(smartphone.getId(), 100));
    }

    @Nested
    @DisplayName("Get All Categories")
    class GetAllCategories {

        @Test
        @DisplayName("should return all categories")
        void shouldReturnAllCategories() throws Exception {
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should return category with required fields")
        void shouldReturnCategoryWithRequiredFields() throws Exception {
            mockMvc.perform(get("/api/v1/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].description").exists());
        }
    }

    @Nested
    @DisplayName("Browse Products by Category")
    class BrowseProductsByCategory {

        @Test
        @DisplayName("should return products in specified category")
        void shouldReturnProductsInCategory() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("categoryId", electronicsCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("should return empty list for category with no products")
        void shouldReturnEmptyListForEmptyCategory() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("categoryId", clothingCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("should return product details with stock info")
        void shouldReturnProductDetailsWithStockInfo() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("categoryId", electronicsCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].price").exists())
                    .andExpect(jsonPath("$[0].stockInfo").exists())
                    .andExpect(jsonPath("$[0].stockInfo.inStock").value(true));
        }
    }

    @Nested
    @DisplayName("Browse Products Without Category Filter")
    class BrowseAllProducts {

        @Test
        @DisplayName("should return paginated product list")
        void shouldReturnPaginatedProductList() throws Exception {
            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.pageSize").value(20));
        }

        @Test
        @DisplayName("should respect pagination parameters")
        void shouldRespectPaginationParameters() throws Exception {
            mockMvc.perform(get("/api/v1/products")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products.length()").value(1))
                    .andExpect(jsonPath("$.pageSize").value(1));
        }
    }

    @Nested
    @DisplayName("Get Category by ID")
    class GetCategoryById {

        @Test
        @DisplayName("should return category details")
        void shouldReturnCategoryDetails() throws Exception {
            mockMvc.perform(get("/api/v1/categories/{id}", electronicsCategory.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(electronicsCategory.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Electronics"))
                    .andExpect(jsonPath("$.description").value("Electronic devices and gadgets"));
        }

        @Test
        @DisplayName("should return 404 for non-existent category")
        void shouldReturn404ForNonExistentCategory() throws Exception {
            mockMvc.perform(get("/api/v1/categories/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
        }
    }
}
