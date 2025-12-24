package com.ecommerce.product.contract;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.dto.ProductSearchResult;
import com.ecommerce.product.application.usecases.BrowseProductsUseCase;
import com.ecommerce.product.application.usecases.GetProductDetailUseCase;
import com.ecommerce.product.application.usecases.SearchProductsUseCase;
import com.ecommerce.product.infrastructure.web.GlobalExceptionHandler;
import com.ecommerce.product.infrastructure.web.controllers.ProductController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/products endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class})
@DisplayName("Product List Contract Tests")
class ProductListContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrowseProductsUseCase browseProductsUseCase;

    @MockBean
    private SearchProductsUseCase searchProductsUseCase;

    @MockBean
    private GetProductDetailUseCase getProductDetailUseCase;

    private static final String PRODUCTS_ENDPOINT = "/api/v1/products";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept request without parameters")
        void shouldAcceptRequestWithoutParameters() throws Exception {
            when(browseProductsUseCase.execute(anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should accept request with pagination parameters")
        void shouldAcceptRequestWithPaginationParameters() throws Exception {
            when(browseProductsUseCase.execute(eq(1), eq(10)))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should accept request with category filter")
        void shouldAcceptRequestWithCategoryFilter() throws Exception {
            UUID categoryId = UUID.randomUUID();
            when(browseProductsUseCase.executeByCategory(categoryId))
                    .thenReturn(List.of(createMockProductResponse()));

            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should use default pagination when not specified")
        void shouldUseDefaultPaginationWhenNotSpecified() throws Exception {
            when(browseProductsUseCase.execute(eq(0), eq(20)))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Response Contract - Paginated List")
    class PaginatedResponseContract {

        @Test
        @DisplayName("should return 200 OK with product list")
        void shouldReturn200WithProductList() throws Exception {
            when(browseProductsUseCase.execute(anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return paginated response structure")
        void shouldReturnPaginatedResponseStructure() throws Exception {
            when(browseProductsUseCase.execute(anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.currentPage").isNumber())
                    .andExpect(jsonPath("$.pageSize").isNumber());
        }

        @Test
        @DisplayName("should return product with required fields")
        void shouldReturnProductWithRequiredFields() throws Exception {
            when(browseProductsUseCase.execute(anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].id").exists())
                    .andExpect(jsonPath("$.products[0].sku").exists())
                    .andExpect(jsonPath("$.products[0].name").exists())
                    .andExpect(jsonPath("$.products[0].price").isNumber())
                    .andExpect(jsonPath("$.products[0].currency").exists())
                    .andExpect(jsonPath("$.products[0].active").isBoolean());
        }

        @Test
        @DisplayName("should return empty list when no products")
        void shouldReturnEmptyListWhenNoProducts() throws Exception {
            when(browseProductsUseCase.execute(anyInt(), anyInt()))
                    .thenReturn(ProductSearchResult.of(List.of(), 0, 0, 20));

            mockMvc.perform(get(PRODUCTS_ENDPOINT))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("Response Contract - Category Filtered List")
    class CategoryFilteredResponseContract {

        @Test
        @DisplayName("should return list when filtering by category")
        void shouldReturnListWhenFilteringByCategory() throws Exception {
            UUID categoryId = UUID.randomUUID();
            when(browseProductsUseCase.executeByCategory(categoryId))
                    .thenReturn(List.of(createMockProductResponse()));

            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").exists());
        }

        @Test
        @DisplayName("should return empty array when category has no products")
        void shouldReturnEmptyArrayWhenCategoryHasNoProducts() throws Exception {
            UUID categoryId = UUID.randomUUID();
            when(browseProductsUseCase.executeByCategory(categoryId))
                    .thenReturn(List.of());

            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Error Response Contract")
    class ErrorResponseContract {

        @Test
        @DisplayName("should handle invalid category ID format")
        void shouldHandleInvalidCategoryIdFormat() throws Exception {
            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("categoryId", "invalid-uuid"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should handle invalid page number")
        void shouldHandleInvalidPageNumber() throws Exception {
            mockMvc.perform(get(PRODUCTS_ENDPOINT)
                            .param("page", "not-a-number"))
                    .andExpect(status().is4xxClientError());
        }
    }

    private ProductSearchResult createMockSearchResult() {
        return ProductSearchResult.of(
                List.of(createMockProductResponse()),
                1,
                0,
                20
        );
    }

    private ProductResponse createMockProductResponse() {
        return new ProductResponse(
                UUID.randomUUID(),
                "SKU-001",
                "Test Product",
                "Test Description",
                new BigDecimal("99.99"),
                "TWD",
                UUID.randomUUID(),
                true,
                List.of("https://example.com/image.jpg"),
                new ProductResponse.StockInfo(100, true, false),
                Instant.now()
        );
    }
}
