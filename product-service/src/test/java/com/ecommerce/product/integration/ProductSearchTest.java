package com.ecommerce.product.integration;

import com.ecommerce.product.domain.entities.Category;
import com.ecommerce.product.domain.entities.Inventory;
import com.ecommerce.product.domain.entities.Product;
import com.ecommerce.product.domain.ports.CategoryRepository;
import com.ecommerce.product.domain.ports.InventoryRepository;
import com.ecommerce.product.domain.ports.ProductRepository;
import com.ecommerce.product.domain.ports.ProductSearchPort;
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
 * Integration tests for product search functionality.
 * Uses in-memory search adapter for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Product Search Integration Tests")
class ProductSearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductSearchPort productSearchPort;

    private Category electronicsCategory;
    private Product gamingLaptop;
    private Product businessLaptop;
    private Product smartphone;

    @BeforeEach
    void setUp() {
        // Create category
        electronicsCategory = Category.create("Electronics", "Electronic devices", null);
        categoryRepository.save(electronicsCategory);

        // Create products with searchable names
        gamingLaptop = Product.create(
                SKU.of("LAPTOP-GAMING-001"),
                "Gaming Laptop Pro",
                "High-end gaming laptop with RTX 4090",
                Money.of(new BigDecimal("89999.00")),
                electronicsCategory.getId()
        );

        businessLaptop = Product.create(
                SKU.of("LAPTOP-BIZ-001"),
                "Business Laptop Elite",
                "Professional business laptop",
                Money.of(new BigDecimal("45999.00")),
                electronicsCategory.getId()
        );

        smartphone = Product.create(
                SKU.of("PHONE-001"),
                "Smartphone Ultra",
                "Latest flagship smartphone",
                Money.of(new BigDecimal("35999.00")),
                electronicsCategory.getId()
        );

        productRepository.save(gamingLaptop);
        productRepository.save(businessLaptop);
        productRepository.save(smartphone);

        // Index products for search
        productSearchPort.index(gamingLaptop);
        productSearchPort.index(businessLaptop);
        productSearchPort.index(smartphone);

        // Create inventory
        inventoryRepository.save(Inventory.create(gamingLaptop.getId(), 25));
        inventoryRepository.save(Inventory.create(businessLaptop.getId(), 50));
        inventoryRepository.save(Inventory.create(smartphone.getId(), 100));
    }

    @Nested
    @DisplayName("Basic Search")
    class BasicSearch {

        @Test
        @DisplayName("should search products by keyword")
        void shouldSearchProductsByKeyword() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("should return empty results for no matches")
        void shouldReturnEmptyResultsForNoMatches() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("should search by partial keyword")
        void shouldSearchByPartialKeyword() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "gaming"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.products[0].name").value("Gaming Laptop Pro"));
        }
    }

    @Nested
    @DisplayName("Search with Category Filter")
    class SearchWithCategoryFilter {

        @Test
        @DisplayName("should search within specific category")
        void shouldSearchWithinCategory() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "laptop")
                            .param("categoryId", electronicsCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("should return empty when no products in category match")
        void shouldReturnEmptyWhenNoCategoryMatch() throws Exception {
            // Create empty category
            Category emptyCategory = Category.create("Empty", "No products", null);
            categoryRepository.save(emptyCategory);

            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "laptop")
                            .param("categoryId", emptyCategory.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isEmpty());
        }
    }

    @Nested
    @DisplayName("Search Pagination")
    class SearchPagination {

        @Test
        @DisplayName("should paginate search results")
        void shouldPaginateSearchResults() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "laptop")
                            .param("page", "0")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.currentPage").value(0))
                    .andExpect(jsonPath("$.pageSize").value(1));
        }

        @Test
        @DisplayName("should return correct page")
        void shouldReturnCorrectPage() throws Exception {
            mockMvc.perform(get("/api/v1/products/search")
                            .param("keyword", "laptop")
                            .param("page", "1")
                            .param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products.length()").value(1))
                    .andExpect(jsonPath("$.currentPage").value(1));
        }
    }

    @Nested
    @DisplayName("Search Suggestions")
    class SearchSuggestions {

        @Test
        @DisplayName("should return search suggestions")
        void shouldReturnSearchSuggestions() throws Exception {
            mockMvc.perform(get("/api/v1/products/suggestions")
                            .param("prefix", "lap"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("should limit suggestion results")
        void shouldLimitSuggestionResults() throws Exception {
            mockMvc.perform(get("/api/v1/products/suggestions")
                            .param("prefix", "lap")
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Get Product Detail")
    class GetProductDetail {

        @Test
        @DisplayName("should return product details with stock info")
        void shouldReturnProductDetailsWithStockInfo() throws Exception {
            mockMvc.perform(get("/api/v1/products/{id}", gamingLaptop.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(gamingLaptop.getId().toString()))
                    .andExpect(jsonPath("$.name").value("Gaming Laptop Pro"))
                    .andExpect(jsonPath("$.sku").value("LAPTOP-GAMING-001"))
                    .andExpect(jsonPath("$.price").value(89999.00))
                    .andExpect(jsonPath("$.stockInfo.available").value(25))
                    .andExpect(jsonPath("$.stockInfo.inStock").value(true));
        }

        @Test
        @DisplayName("should return 404 for non-existent product")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"));
        }
    }
}
