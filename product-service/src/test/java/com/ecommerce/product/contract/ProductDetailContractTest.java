package com.ecommerce.product.contract;

import com.ecommerce.product.application.dto.ProductResponse;
import com.ecommerce.product.application.exceptions.ProductNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/products/{id} endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class})
@DisplayName("Product Detail Contract Tests")
class ProductDetailContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrowseProductsUseCase browseProductsUseCase;

    @MockBean
    private SearchProductsUseCase searchProductsUseCase;

    @MockBean
    private GetProductDetailUseCase getProductDetailUseCase;

    private static final String PRODUCT_DETAIL_ENDPOINT = "/api/v1/products/{productId}";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should accept valid product ID")
        void shouldAcceptValidProductId() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(createMockProductResponse(productId));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should handle invalid UUID format")
        void shouldHandleInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/v1/products/invalid-uuid"))
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Response Contract - Success")
    class SuccessResponseContract {

        @Test
        @DisplayName("should return 200 OK with product details")
        void shouldReturn200WithProductDetails() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(createMockProductResponse(productId));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return product with all required fields")
        void shouldReturnProductWithAllRequiredFields() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(createMockProductResponse(productId));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(productId.toString()))
                    .andExpect(jsonPath("$.sku").isString())
                    .andExpect(jsonPath("$.name").isString())
                    .andExpect(jsonPath("$.description").isString())
                    .andExpect(jsonPath("$.price").isNumber())
                    .andExpect(jsonPath("$.currency").isString())
                    .andExpect(jsonPath("$.categoryId").isString())
                    .andExpect(jsonPath("$.active").isBoolean())
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        @DisplayName("should return product with stock info")
        void shouldReturnProductWithStockInfo() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(createMockProductResponse(productId));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stockInfo").exists())
                    .andExpect(jsonPath("$.stockInfo.available").isNumber())
                    .andExpect(jsonPath("$.stockInfo.inStock").isBoolean())
                    .andExpect(jsonPath("$.stockInfo.lowStock").isBoolean());
        }

        @Test
        @DisplayName("should return product with image URLs")
        void shouldReturnProductWithImageUrls() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(createMockProductResponse(productId));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.imageUrls").isArray())
                    .andExpect(jsonPath("$.imageUrls[0]").isString());
        }

        @Test
        @DisplayName("should handle product without stock info")
        void shouldHandleProductWithoutStockInfo() throws Exception {
            UUID productId = UUID.randomUUID();
            ProductResponse responseWithoutStock = new ProductResponse(
                    productId,
                    "SKU-001",
                    "Test Product",
                    "Description",
                    new BigDecimal("99.99"),
                    "TWD",
                    UUID.randomUUID(),
                    true,
                    List.of(),
                    null,
                    Instant.now()
            );
            when(getProductDetailUseCase.execute(productId))
                    .thenReturn(responseWithoutStock);

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.stockInfo").doesNotExist());
        }
    }

    @Nested
    @DisplayName("Response Contract - Failure")
    class FailureResponseContract {

        @Test
        @DisplayName("should return 404 Not Found when product does not exist")
        void shouldReturn404WhenProductNotFound() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(productId))
                    .thenThrow(ProductNotFoundException.byId(productId.toString()));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("PRODUCT_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Error Response Contract")
    class ErrorResponseContract {

        @Test
        @DisplayName("should return standard error structure for not found")
        void shouldReturnStandardErrorStructureForNotFound() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(any(UUID.class)))
                    .thenThrow(ProductNotFoundException.byId(productId.toString()));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("should not leak internal error details")
        void shouldNotLeakInternalErrorDetails() throws Exception {
            UUID productId = UUID.randomUUID();
            when(getProductDetailUseCase.execute(any(UUID.class)))
                    .thenThrow(ProductNotFoundException.byId(productId.toString()));

            mockMvc.perform(get(PRODUCT_DETAIL_ENDPOINT, productId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.cause").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist());
        }
    }

    private ProductResponse createMockProductResponse(UUID productId) {
        return new ProductResponse(
                productId,
                "SKU-TEST-001",
                "Premium Gaming Laptop",
                "High-end gaming laptop with RTX 4090",
                new BigDecimal("89999.00"),
                "TWD",
                UUID.randomUUID(),
                true,
                List.of(
                        "https://example.com/laptop1.jpg",
                        "https://example.com/laptop2.jpg"
                ),
                new ProductResponse.StockInfo(25, true, false),
                Instant.now()
        );
    }
}
