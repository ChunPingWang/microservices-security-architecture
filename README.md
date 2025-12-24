# E-Commerce Microservices Platform

企業級電商微服務平台，採用領域驅動設計 (DDD) 與六角架構 (Hexagonal Architecture) 實作。

## 系統架構

```
┌─────────────────────────────────────────────────────────────────┐
│                        API Gateway                               │
│                    (Rate Limiting, Auth)                         │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│   Customer    │     │    Product    │     │     Order     │
│   Service     │     │    Service    │     │    Service    │
│   (8081)      │     │    (8082)     │     │    (8083)     │
└───────────────┘     └───────────────┘     └───────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│    Payment    │     │   Logistics   │     │     Sales     │
│   Service     │     │    Service    │     │    Service    │
│   (8084)      │     │    (8084)     │     │    (8085)     │
└───────────────┘     └───────────────┘     └───────────────┘
                                │
                                ▼
                    ┌───────────────────┐
                    │   Admin Portal    │
                    │     (8090)        │
                    └───────────────────┘
```

## 功能特性

### 客戶端功能 (US1-US7)
- **US1 客戶認證**: 註冊、登入、JWT 認證、帳號鎖定
- **US2 商品瀏覽**: 商品列表、分類、搜尋、詳情頁
- **US3 購物車**: 新增、修改數量、移除商品
- **US4 訂單付款**: 建立訂單、多種付款方式、付款逾時處理
- **US5 訂單追蹤**: 物流追蹤、狀態更新通知
- **US6 促銷優惠**: 促銷活動、優惠券驗證與套用
- **US7 會員等級**: 消費累積、等級升級、會員專屬折扣

### 管理後台功能
- 訂單管理 (列表、狀態更新、取消)
- 商品管理 (庫存調整、上下架)
- 促銷管理 (CRUD、啟停用)
- 客戶管理 (帳號狀態、會員等級調整)
- 報表功能 (銷售報表、每日銷售、熱銷商品、客戶統計)

## 技術架構

### 後端技術
| 技術 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 程式語言 |
| Spring Boot | 3.2.1 | 應用框架 |
| Spring Security | 6.x | 安全認證 |
| Spring Data JPA | 3.x | 資料存取 |
| PostgreSQL | 15 | 主要資料庫 |
| Redis | 7 | 快取與會話 |
| Flyway | 10.x | 資料庫遷移 |

### 可觀測性
| 技術 | 用途 |
|------|------|
| Micrometer | 指標收集 |
| Zipkin | 分散式追蹤 |
| Prometheus | 指標儲存 |
| Grafana | 指標視覺化 |
| Spring Actuator | 健康檢查 |

### 基礎設施
| 技術 | 用途 |
|------|------|
| Docker | 容器化 |
| Kubernetes | 容器編排 |
| Nginx Ingress | API 路由 |

## 微服務安全架構

本系統採用 Spring Security 實現完整的微服務安全防護，針對不同流量方向採用不同的安全策略。

### 流量方向說明

```
                    ┌─────────────────────────────────────────┐
                    │              Internet                    │
                    └─────────────────┬───────────────────────┘
                                      │ 南北向流量 (North-South)
                                      │ 外部客戶端 → API Gateway
                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                           API Gateway                                    │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter: 驗證 JWT Token                         │   │
│  │  SecurityConfig: 路徑授權規則                                     │   │
│  │  Rate Limiting: 流量限制                                          │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────┬───────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │  東西向流量 (East-West)
          │                   │                   │  服務間內部通訊
          ▼                   ▼                   ▼
    ┌──────────┐        ┌──────────┐        ┌──────────┐
    │ Customer │◄──────►│ Product  │◄──────►│  Order   │
    │ Service  │        │ Service  │        │ Service  │
    └──────────┘        └──────────┘        └──────────┘
          │                   │                   │
          │     ServiceAuthInterceptor            │
          │     (服務間 JWT 認證)                  │
          ▼                   ▼                   ▼
    ┌──────────┐        ┌──────────┐        ┌──────────┐
    │ Payment  │◄──────►│Logistics │◄──────►│  Sales   │
    │ Service  │        │ Service  │        │ Service  │
    └──────────┘        └──────────┘        └──────────┘
```

### 南北向安全 (North-South Traffic)

南北向流量指外部客戶端（如行動 App、Web 前端）透過 API Gateway 進入系統的請求。

#### 認證流程

```
Client                    API Gateway                Service
  │                           │                         │
  │  1. POST /api/auth/login  │                         │
  │  ─────────────────────────►                         │
  │                           │                         │
  │  2. JWT Token (Access + Refresh)                    │
  │  ◄─────────────────────────                         │
  │                           │                         │
  │  3. Request + Authorization: Bearer <token>         │
  │  ─────────────────────────►                         │
  │                           │                         │
  │                     4. JwtAuthenticationFilter      │
  │                        驗證 Token                    │
  │                           │                         │
  │                     5. 設定 SecurityContext          │
  │                           │                         │
  │                           │  6. 轉發請求 + 用戶資訊   │
  │                           │  ────────────────────────►
  │                           │                         │
  │  7. Response              │                         │
  │  ◄─────────────────────────────────────────────────
```

#### JwtAuthenticationFilter 實作

```java
// security-infrastructure/src/main/java/com/ecommerce/security/filter/JwtAuthenticationFilter.java

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CurrentUserContext currentUserContext;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 從 Header 提取 JWT Token
            extractToken(request).ifPresent(this::authenticateToken);
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return Optional.of(bearerToken.substring(7));
        }
        return Optional.empty();
    }

    private void authenticateToken(String token) {
        // 驗證 Token 並取得 Claims
        jwtTokenProvider.validateToken(token).ifPresent(claims -> {
            String userId = claims.get("userId", String.class);
            String email = claims.get("email", String.class);
            String rolesStr = claims.get("roles", String.class);

            // 設定 Spring Security Context
            List<SimpleGrantedAuthority> authorities = parseRoles(rolesStr);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 設定當前用戶上下文 (供業務層使用)
            currentUserContext.setCurrentUser(userId, email, rolesStr);
        });
    }
}
```

#### SecurityConfig 配置

```java
// api-gateway/src/main/java/com/ecommerce/gateway/config/SecurityConfig.java

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公開端點
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()

                // 需要認證的端點
                .requestMatchers("/api/v1/cart/**").authenticated()
                .requestMatchers("/api/v1/orders/**").authenticated()

                // 管理員專用端點
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

### 東西向安全 (East-West Traffic)

東西向流量指微服務之間的內部通訊，例如 Order Service 呼叫 Payment Service。

#### 服務間認證機制

```
Order Service                                    Payment Service
      │                                                │
      │  1. 準備呼叫 Payment Service                     │
      │                                                │
      │  2. ServiceAuthInterceptor 注入服務 Token        │
      │     Authorization: Bearer <service-token>       │
      │                                                │
      │  3. Feign Client 發送請求                        │
      │  ─────────────────────────────────────────────► │
      │                                                │
      │                    4. JwtAuthenticationFilter   │
      │                       驗證服務 Token             │
      │                                                │
      │                    5. 檢查 ROLE_SERVICE 權限     │
      │                                                │
      │  6. Response                                    │
      │  ◄───────────────────────────────────────────── │
```

#### ServiceAuthInterceptor 實作

```java
// security-infrastructure/src/main/java/com/ecommerce/security/interceptor/ServiceAuthInterceptor.java

@Component
public class ServiceAuthInterceptor implements RequestInterceptor {

    private static final String SERVICE_USER_ID = "service-internal";
    private static final String SERVICE_EMAIL = "service@internal";
    private static final String SERVICE_ROLE = "SERVICE";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void apply(RequestTemplate template) {
        // 為服務間呼叫產生專用 JWT Token
        String serviceToken = jwtTokenProvider.generateAccessToken(
            SERVICE_USER_ID,
            SERVICE_EMAIL,
            SERVICE_ROLE
        );

        template.header("Authorization", "Bearer " + serviceToken);
    }
}
```

#### Feign Client 配置

```java
// 服務呼叫端配置

@FeignClient(
    name = "payment-service",
    configuration = FeignClientConfig.class
)
public interface PaymentServiceClient {

    @PostMapping("/internal/payments/process")
    PaymentResult processPayment(@RequestBody PaymentRequest request);
}

@Configuration
public class FeignClientConfig {

    @Bean
    public ServiceAuthInterceptor serviceAuthInterceptor(
            JwtTokenProvider jwtTokenProvider) {
        return new ServiceAuthInterceptor(jwtTokenProvider);
    }
}
```

### 用戶上下文傳遞

在微服務架構中，用戶資訊需要在服務間傳遞。本系統採用 `CurrentUserContext` 實現：

```java
// security-infrastructure/src/main/java/com/ecommerce/security/context/CurrentUserContext.java

@Component
@RequestScope
public class CurrentUserContext {

    private String userId;
    private String email;
    private String roles;

    public void setCurrentUser(String userId, String email, String roles) {
        this.userId = userId;
        this.email = email;
        this.roles = roles;
    }

    public String getCurrentUserId() {
        return userId;
    }

    // 在業務層使用
    // @Autowired CurrentUserContext currentUser;
    // String userId = currentUser.getCurrentUserId();
}
```

### AOP 授權檢查

除了基於路徑的授權，系統還支援方法級別的授權：

```java
// 使用 @PreAuthorize 進行方法級授權
@Service
public class OrderService {

    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public Order createOrder(CreateOrderCommand command) {
        // ...
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void cancelOrder(String orderId) {
        // ...
    }
}
```

### 分散式追蹤整合

安全元件與分散式追蹤整合，確保請求可被追蹤：

```java
// security-infrastructure/src/main/java/com/ecommerce/security/tracing/TracingFeignInterceptor.java

@Component
public class TracingFeignInterceptor implements RequestInterceptor {

    private final Tracer tracer;

    @Override
    public void apply(RequestTemplate template) {
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            // 傳遞 B3 追蹤標頭
            template.header("X-B3-TraceId", currentSpan.context().traceId());
            template.header("X-B3-SpanId", currentSpan.context().spanId());
            template.header("X-B3-Sampled", "1");
        }
    }
}
```

### 安全最佳實踐

| 實踐項目 | 說明 |
|---------|------|
| 無狀態認證 | 使用 JWT，不依賴 Session |
| Token 過期 | Access Token 15分鐘，Refresh Token 7天 |
| 密碼加密 | 使用 BCrypt 加密儲存 |
| 帳號鎖定 | 連續登入失敗 5 次鎖定 30 分鐘 |
| HTTPS | 生產環境強制使用 HTTPS |
| 服務隔離 | 內部服務使用專用 Service Token |
| 追蹤整合 | 安全事件納入分散式追蹤 |

### 測試安全元件

```java
// 使用 Mock 進行安全測試

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerSecurityTest {

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void authenticatedUser_canCreateOrder() {
        // 模擬已認證用戶
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isCreated());
    }

    @Test
    void unauthenticatedUser_cannotCreateOrder() {
        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
            .andExpect(status().isUnauthorized());
    }
}
```

> 📘 **詳細說明**: 完整的安全架構規格文件請參考 [spring-security-microservices-architecture.md](./spring-security-microservices-architecture.md)

## 快速開始

### 環境需求
- Java 21+
- Docker & Docker Compose
- Gradle 8.5+

### 啟動基礎設施

```bash
# 啟動 PostgreSQL, Redis, Elasticsearch, Zipkin
cd infrastructure/docker
docker-compose up -d

# 啟動完整監控 (含 Prometheus + Grafana)
docker-compose --profile monitoring up -d
```

### 建置與測試

```bash
# 建置所有模組
./gradlew build

# 執行測試
./gradlew test

# 執行特定服務測試
./gradlew :customer-service:test
```

### 啟動服務

```bash
# 啟動 Customer Service
./gradlew :customer-service:bootRun

# 啟動 Product Service
./gradlew :product-service:bootRun

# 啟動其他服務...
```

## 專案結構

```
microservices-security-architecture/
├── shared-kernel/              # 共用領域物件
├── security-infrastructure/    # 安全基礎設施 (JWT, Auth)
├── customer-service/           # 客戶服務 (port: 8081)
├── product-service/            # 商品服務 (port: 8082)
├── order-service/              # 訂單服務 (port: 8083)
├── payment-service/            # 付款服務 (port: 8084)
├── logistics-service/          # 物流服務 (port: 8084)
├── sales-service/              # 促銷服務 (port: 8085)
├── admin-portal/
│   └── backend/                # 管理後台 (port: 8090)
├── api-gateway/                # API 閘道
└── infrastructure/
    ├── docker/                 # Docker Compose 配置
    └── k8s/                    # Kubernetes 清單
        ├── base/               # 基礎配置
        └── overlays/           # 環境覆蓋
            ├── dev/
            └── prod/
```

## API 文件

API 規格文件位於: `api-gateway/src/main/resources/openapi/api-spec.yaml`

### 主要端點

| 服務 | 端點 | 說明 |
|------|------|------|
| 認證 | `POST /api/auth/login` | 客戶登入 |
| 客戶 | `POST /api/customers/register` | 客戶註冊 |
| 客戶 | `GET /api/customers/me` | 取得個人資料 |
| 客戶 | `GET /api/customers/me/membership` | 取得會員資訊 |
| 商品 | `GET /api/v1/products` | 商品列表 |
| 商品 | `GET /api/v1/products/{id}` | 商品詳情 |
| 商品 | `GET /api/v1/products/search` | 商品搜尋 |
| 購物車 | `GET /api/v1/cart` | 取得購物車 |
| 購物車 | `POST /api/v1/cart/items` | 新增商品 |
| 訂單 | `POST /api/v1/orders` | 建立訂單 |
| 訂單 | `GET /api/v1/orders` | 訂單歷史 |
| 物流 | `GET /api/v1/shipments/{orderId}/tracking` | 物流追蹤 |
| 促銷 | `GET /api/v1/promotions` | 促銷活動 |
| 促銷 | `POST /api/v1/coupons/validate` | 優惠券驗證 |

### 管理端點

| 端點 | 說明 |
|------|------|
| `POST /api/admin/auth/login` | 管理員登入 |
| `GET /api/admin/products` | 商品列表 |
| `GET /api/admin/orders` | 訂單列表 |
| `GET /api/admin/customers` | 客戶列表 |
| `GET /api/admin/promotions` | 促銷列表 |
| `GET /api/admin/reports/sales` | 銷售報表 |

## Kubernetes 部署

```bash
# 開發環境部署
kubectl apply -k infrastructure/k8s/overlays/dev/

# 生產環境部署
kubectl apply -k infrastructure/k8s/overlays/prod/
```

## 可觀測性

### 健康檢查

所有服務提供以下 Actuator 端點:

```
GET /actuator/health           # 健康狀態
GET /actuator/health/liveness  # K8s Liveness Probe
GET /actuator/health/readiness # K8s Readiness Probe
GET /actuator/info             # 服務資訊
GET /actuator/metrics          # 指標
GET /actuator/prometheus       # Prometheus 格式指標
```

### 分散式追蹤

- **Zipkin UI**: http://localhost:9411
- 追蹤標頭: B3 格式 (`X-B3-TraceId`, `X-B3-SpanId`)

### 監控儀表板

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## 測試

```bash
# 執行所有測試
./gradlew test

# 測試覆蓋率報告
./gradlew jacocoTestReport
```

**測試統計**: 478 tests passing

| 服務 | 測試數 |
|------|--------|
| customer-service | 67 |
| product-service | 89 |
| order-service | 42 |
| payment-service | 58 |
| logistics-service | 45 |
| sales-service | 48 |
| admin-portal | 50 |
| shared-kernel | 79 |

## 開發規範

### 架構原則
- **DDD (Domain-Driven Design)**: 領域驅動設計
- **Hexagonal Architecture**: 六角架構 (Ports & Adapters)
- **TDD (Test-Driven Development)**: 測試驅動開發

### 程式碼品質
- Checkstyle: 程式碼風格檢查
- SpotBugs: 靜態分析

### 提交規範

```
功能: <簡短描述>

<詳細說明>

測試: <測試數量> tests passing
```

## 授權

MIT License

## 貢獻者

- E-Commerce Team
