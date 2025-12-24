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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contract tests for GET /api/v1/products/search endpoint.
 */
@WebMvcTest(excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = {ProductController.class, GlobalExceptionHandler.class})
@DisplayName("Product Search Contract Tests")
class ProductSearchContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrowseProductsUseCase browseProductsUseCase;

    @MockBean
    private SearchProductsUseCase searchProductsUseCase;

    @MockBean
    private GetProductDetailUseCase getProductDetailUseCase;

    private static final String SEARCH_ENDPOINT = "/api/v1/products/search";
    private static final String SUGGESTIONS_ENDPOINT = "/api/v1/products/suggestions";

    @Nested
    @DisplayName("Request Contract")
    class RequestContract {

        @Test
        @DisplayName("should require keyword parameter")
        void shouldRequireKeywordParameter() throws Exception {
            mockMvc.perform(get(SEARCH_ENDPOINT))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should accept search with keyword only")
        void shouldAcceptSearchWithKeywordOnly() throws Exception {
            when(searchProductsUseCase.execute(anyString(), anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should accept search with pagination")
        void shouldAcceptSearchWithPagination() throws Exception {
            when(searchProductsUseCase.execute(eq("laptop"), eq(2), eq(15)))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop")
                            .param("page", "2")
                            .param("size", "15"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should accept search with category filter")
        void shouldAcceptSearchWithCategoryFilter() throws Exception {
            UUID categoryId = UUID.randomUUID();
            when(searchProductsUseCase.executeInCategory(eq("laptop"), eq(categoryId), anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop")
                            .param("categoryId", categoryId.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should use default pagination when not specified")
        void shouldUseDefaultPaginationWhenNotSpecified() throws Exception {
            when(searchProductsUseCase.execute(eq("laptop"), eq(0), eq(20)))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Response Contract")
    class ResponseContract {

        @Test
        @DisplayName("should return 200 OK with search results")
        void shouldReturn200WithSearchResults() throws Exception {
            when(searchProductsUseCase.execute(anyString(), anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("should return paginated search result structure")
        void shouldReturnPaginatedSearchResultStructure() throws Exception {
            when(searchProductsUseCase.execute(anyString(), anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.totalPages").isNumber())
                    .andExpect(jsonPath("$.currentPage").isNumber())
                    .andExpect(jsonPath("$.pageSize").isNumber());
        }

        @Test
        @DisplayName("should return product with required fields in search results")
        void shouldReturnProductWithRequiredFieldsInSearchResults() throws Exception {
            when(searchProductsUseCase.execute(anyString(), anyInt(), anyInt()))
                    .thenReturn(createMockSearchResult());

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products[0].id").exists())
                    .andExpect(jsonPath("$.products[0].sku").exists())
                    .andExpect(jsonPath("$.products[0].name").exists())
                    .andExpect(jsonPath("$.products[0].price").isNumber())
                    .andExpect(jsonPath("$.products[0].currency").exists());
        }

        @Test
        @DisplayName("should return empty results when no matches")
        void shouldReturnEmptyResultsWhenNoMatches() throws Exception {
            when(searchProductsUseCase.execute(anyString(), anyInt(), anyInt()))
                    .thenReturn(ProductSearchResult.of(List.of(), 0, 0, 20));

            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.products").isArray())
                    .andExpect(jsonPath("$.products").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("Suggestions Endpoint Contract")
    class SuggestionsContract {

        @Test
        @DisplayName("should require prefix parameter for suggestions")
        void shouldRequirePrefixParameter() throws Exception {
            mockMvc.perform(get(SUGGESTIONS_ENDPOINT))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should return suggestions as string array")
        void shouldReturnSuggestionsAsStringArray() throws Exception {
            when(searchProductsUseCase.getSuggestions(anyString(), anyInt()))
                    .thenReturn(List.of("laptop", "laptop bag", "laptop stand"));

            mockMvc.perform(get(SUGGESTIONS_ENDPOINT)
                            .param("prefix", "lap"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0]").isString());
        }

        @Test
        @DisplayName("should return empty array when no suggestions")
        void shouldReturnEmptyArrayWhenNoSuggestions() throws Exception {
            when(searchProductsUseCase.getSuggestions(anyString(), anyInt()))
                    .thenReturn(List.of());

            mockMvc.perform(get(SUGGESTIONS_ENDPOINT)
                            .param("prefix", "xyz"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("should accept limit parameter for suggestions")
        void shouldAcceptLimitParameter() throws Exception {
            when(searchProductsUseCase.getSuggestions(eq("lap"), eq(5)))
                    .thenReturn(List.of("laptop"));

            mockMvc.perform(get(SUGGESTIONS_ENDPOINT)
                            .param("prefix", "lap")
                            .param("limit", "5"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Error Response Contract")
    class ErrorResponseContract {

        @Test
        @DisplayName("should handle invalid category ID format")
        void shouldHandleInvalidCategoryIdFormat() throws Exception {
            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop")
                            .param("categoryId", "invalid-uuid"))
                    .andExpect(status().is4xxClientError());
        }

        @Test
        @DisplayName("should handle invalid page number")
        void shouldHandleInvalidPageNumber() throws Exception {
            mockMvc.perform(get(SEARCH_ENDPOINT)
                            .param("keyword", "laptop")
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
                "Gaming Laptop",
                "High performance gaming laptop",
                new BigDecimal("29999.00"),
                "TWD",
                UUID.randomUUID(),
                true,
                List.of("https://example.com/laptop.jpg"),
                new ProductResponse.StockInfo(50, true, false),
                Instant.now()
        );
    }
}
