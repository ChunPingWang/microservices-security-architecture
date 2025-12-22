# 微服務安全架構設計規格書

## 不使用 Service Mesh 的 Spring Security 解決方案

**版本：** 1.0  
**日期：** 2025-12-21  
**適用範圍：** Java/Spring Boot 微服務架構  
**目標讀者：** 架構師、後端開發人員、DevOps 工程師  
**相關標準：** OAuth 2.0, OIDC, OWASP, PCI-DSS

---

## 目錄

1. [架構概述](#1-架構概述)
2. [系統架構](#2-系統架構)
3. [南北向安全設計](#3-南北向安全設計)
4. [東西向安全設計](#4-東西向安全設計)
5. [AOP 授權切面](#5-aop-授權切面)
6. [業務層使用範例](#6-業務層使用範例)
7. [環境配置與測試隔離](#7-環境配置與測試隔離)
8. [方案比較與選型建議](#8-方案比較與選型建議)
9. [附錄](#9-附錄)

---

## 1. 架構概述

### 1.1 設計目標

本規格書定義了一套不依賴 Service Mesh（如 Istio）的微服務安全架構，透過 Spring Security 與 AOP 機制實現：

- **南北向（North-South）**：外部請求的認證與授權
- **東西向（East-West）**：服務間呼叫的身份驗證與授權
- **安全邏輯與業務邏輯完全解耦**
- **開發、測試、生產環境的安全配置隔離**

### 1.2 適用場景

| 推薦使用本方案 | 建議使用 Service Mesh |
|---------------|---------------------|
| 服務數量 < 50 | 服務數量 > 100 |
| 團隊熟悉 Spring 生態系 | 多語言微服務環境 |
| 希望降低基礎設施複雜度 | 需要統一的 mTLS 管理 |
| 對延遲敏感（避免 Sidecar 開銷） | 有專職 SRE 團隊維運 |

---

## 2. 系統架構

### 2.1 分層架構圖

系統採用三層式安全架構，將安全基礎設施與業務邏輯完全分離：

```
┌─────────────────────────────────────────────────────────────────────┐
│                    Security Infrastructure Layer                     │
│  ┌────────────────┐ ┌────────────────┐ ┌───────────────────────┐   │
│  │ Spring Security│ │  AOP Aspects   │ │  Feign Interceptors   │   │
│  │   Filters      │ │ (Authorization)│ │  (Token Propagation)  │   │
│  └───────┬────────┘ └───────┬────────┘ └───────────┬───────────┘   │
└──────────┼───────────────────┼───────────────────────┼──────────────┘
           │                   │                       │
           ▼                   ▼                       ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        Business Logic Layer                          │
│                  （完全不知道安全機制存在）                            │
│  ┌────────────────┐ ┌────────────────┐ ┌───────────────────────┐   │
│  │  OrderService  │ │ PaymentService │ │   AccountService      │   │
│  └────────────────┘ └────────────────┘ └───────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 模組結構

```
project-root/
├── security-infrastructure/          # 獨立安全模組
│   ├── src/main/java/
│   │   └── com/example/security/
│   │       ├── config/               # Spring Security 配置
│   │       ├── filter/               # 自定義 Filter
│   │       ├── aspect/               # AOP 切面
│   │       ├── interceptor/          # Feign/RestTemplate 攔截器
│   │       ├── context/              # 安全上下文
│   │       ├── annotation/           # 自定義註解
│   │       └── mock/                 # 測試用 Mock
│   └── src/main/resources/
│       └── META-INF/spring/
│           └── ...AutoConfiguration.imports
│
├── order-service/                    # 業務模組（無安全程式碼）
├── payment-service/
└── account-service/
```

---

## 3. 南北向安全設計

### 3.1 認證流程

南北向認證處理來自外部用戶或第三方系統的請求，使用 OIDC/OAuth 2.0 協定。

```
┌────────┐                    ┌─────────────┐              ┌─────────────┐
│  用戶   │                    │ API Gateway │              │   Service   │
└───┬────┘                    └──────┬──────┘              └──────┬──────┘
    │  ① Bearer Token (JWT)          │                            │
    │───────────────────────────────►│                            │
    │                                │ ② 驗證 Token               │
    │                                │   - 簽章驗證               │
    │                                │   - 過期檢查               │
    │                                │   - Scope/Role 檢查        │
    │                                │                            │
    │                                │ ③ 轉發請求 + 用戶上下文     │
    │                                │───────────────────────────►│
```

### 3.2 Spring Security 配置

#### 3.2.1 SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!test")  // 測試環境不啟用
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) 
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/actuator/health", 
                    "/api/public/**"
                ).permitAll()
                .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter()))
            );
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = 
            new JwtGrantedAuthoritiesConverter();
        converter.setAuthoritiesClaimName("roles");
        converter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtConverter = 
            new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
```

#### 3.2.2 CurrentUserContext 介面

定義抽象介面，讓業務層只依賴介面而非實作：

```java
// 業務層依賴的介面
public interface CurrentUserContext {
    String getUserId();
    String getTenantId();
    Set<String> getRoles();
    Set<String> getPermissions();
    boolean hasPermission(String permission);
}

// 生產環境實作
@Component
@RequestScope
@Profile("!test")
public class JwtBasedUserContext implements CurrentUserContext {
    
    @Override
    public String getUserId() {
        return getJwtClaim("sub");
    }
    
    @Override
    public String getTenantId() {
        return getJwtClaim("tenant_id");
    }
    
    @Override
    public Set<String> getPermissions() {
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return extractPermissions(jwtAuth.getToken());
        }
        return Collections.emptySet();
    }
    
    @Override
    public boolean hasPermission(String permission) {
        return getPermissions().contains(permission);
    }
    
    private String getJwtClaim(String claim) {
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getClaimAsString(claim);
        }
        return null;
    }
}
```

---

## 4. 東西向安全設計

### 4.1 服務間認證模式

| 模式 | 說明 | 適用場景 |
|------|------|----------|
| Service Token | 服務專用身份 Token | 背景任務、系統對系統呼叫 |
| Token Propagation | 傳遞原始用戶 Token | 需要用戶上下文的操作 |
| 混合模式 | Service Token + User Context | 既需服務身份又需用戶資訊 |

### 4.2 Feign 攔截器實作

#### 4.2.1 FeignSecurityInterceptor.java

```java
@Component
@Profile("!test")
public class FeignSecurityInterceptor 
        implements RequestInterceptor {
    
    private final ServiceTokenProvider serviceTokenProvider;
    
    @Override
    public void apply(RequestTemplate template) {
        // 1. 傳遞原始用戶 Token
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            template.header("X-Original-User-Token", 
                jwtAuth.getToken().getTokenValue());
            template.header("X-User-Id", 
                jwtAuth.getToken().getClaimAsString("sub"));
        }
        
        // 2. 加入服務身份 Token
        String serviceToken = 
            serviceTokenProvider.getServiceToken();
        template.header("X-Service-Token", serviceToken);
        template.header("X-Calling-Service", "order-service");
        
        // 3. 傳遞追蹤資訊
        template.header("X-Correlation-Id", 
            MDC.get("correlationId"));
    }
}
```

#### 4.2.2 ServiceTokenProvider.java

```java
@Component
public class ServiceTokenProvider {
    
    private final String serviceId;
    private final String serviceSecret;
    private final TokenCache tokenCache;
    private final RestTemplate restTemplate;
    
    @Value("${security.oauth2.token-endpoint}")
    private String tokenEndpoint;
    
    // 使用 Client Credentials Flow
    public String getServiceToken() {
        return tokenCache.getOrRefresh(
            "service-token", 
            this::fetchNewServiceToken
        );
    }
    
    private String fetchNewServiceToken() {
        MultiValueMap<String, String> params = 
            new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", serviceId);
        params.add("client_secret", serviceSecret);
        params.add("scope", "service-to-service");
        
        TokenResponse response = restTemplate.postForObject(
            tokenEndpoint, params, TokenResponse.class);
        return response.getAccessToken();
    }
}
```

### 4.3 服務端驗證 Filter

#### 4.3.1 ServiceAuthenticationFilter.java

```java
@Component
public class ServiceAuthenticationFilter 
        extends OncePerRequestFilter {
    
    private final ServiceTokenValidator validator;
    private final Set<String> allowedServices;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain chain) 
            throws ServletException, IOException {
        
        String serviceToken = 
            request.getHeader("X-Service-Token");
        String callingService = 
            request.getHeader("X-Calling-Service");
        
        if (serviceToken != null) {
            if (validator.validate(serviceToken) && 
                allowedServices.contains(callingService)) {
                
                ServiceAuthentication serviceAuth = 
                    new ServiceAuthentication(callingService);
                
                // 還原用戶上下文
                String userToken = 
                    request.getHeader("X-Original-User-Token");
                if (userToken != null) {
                    serviceAuth.setOriginalUserContext(
                        extractUserContext(userToken));
                }
                
                SecurityContextHolder.getContext()
                    .setAuthentication(serviceAuth);
            }
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        // 只處理內部服務呼叫路徑
        return !request.getRequestURI()
            .startsWith("/internal/");
    }
}
```

---

## 5. AOP 授權切面

### 5.1 自定義授權註解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String[] value();
    LogicalOperator operator() default LogicalOperator.AND;
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwnership {
    String resourceIdParam() default "id";
    Class<?> resourceType();
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceToService {
    String[] allowedCallers();
}

public enum LogicalOperator {
    AND, OR
}
```

### 5.2 AuthorizationAspect.java

```java
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthorizationAspect {
    
    private final CurrentUserContext userContext;
    private final ResourceOwnershipChecker ownershipChecker;
    
    // 權限檢查
    @Before("@annotation(requirePermission)")
    public void checkPermission(
            JoinPoint joinPoint, 
            RequirePermission requirePermission) {
        
        String[] required = requirePermission.value();
        LogicalOperator op = requirePermission.operator();
        
        boolean authorized = switch (op) {
            case AND -> Arrays.stream(required)
                .allMatch(userContext::hasPermission);
            case OR -> Arrays.stream(required)
                .anyMatch(userContext::hasPermission);
        };
        
        if (!authorized) {
            throw new AccessDeniedException(
                "Missing permissions: " + 
                Arrays.toString(required));
        }
    }
    
    // 資源所有權檢查
    @Before("@annotation(requireOwnership)")
    public void checkOwnership(
            JoinPoint joinPoint, 
            RequireOwnership requireOwnership) {
        
        Object resourceId = extractParameter(
            joinPoint, 
            requireOwnership.resourceIdParam());
        
        boolean isOwner = ownershipChecker.isOwner(
            userContext.getUserId(),
            requireOwnership.resourceType(),
            resourceId
        );
        
        if (!isOwner && 
            !userContext.hasPermission("ADMIN")) {
            throw new AccessDeniedException(
                "User does not own this resource");
        }
    }
    
    // 服務間呼叫檢查
    @Before("@annotation(serviceToService)")
    public void checkServiceCaller(
            JoinPoint joinPoint, 
            ServiceToService serviceToService) {
        
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        
        if (!(auth instanceof ServiceAuthentication)) {
            throw new AccessDeniedException(
                "Service-to-service calls only");
        }
        
        ServiceAuthentication serviceAuth = 
            (ServiceAuthentication) auth;
        String caller = serviceAuth.getCallingService();
        
        if (!Arrays.asList(
                serviceToService.allowedCallers())
                .contains(caller)) {
            throw new AccessDeniedException(
                "Service " + caller + " not allowed");
        }
    }
}
```

### 5.3 AuditLogAspect.java

```java
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class SecurityAuditAspect {
    
    private final AuditLogService auditLogService;
    private final CurrentUserContext userContext;
    
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object auditApiCall(ProceedingJoinPoint joinPoint) 
            throws Throwable {
        
        AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
            .timestamp(Instant.now())
            .userId(userContext.getUserId())
            .tenantId(userContext.getTenantId())
            .action(joinPoint.getSignature().getName())
            .resource(joinPoint.getTarget()
                .getClass().getSimpleName())
            .correlationId(MDC.get("correlationId"));
        
        try {
            Object result = joinPoint.proceed();
            logBuilder.status("SUCCESS");
            return result;
        } catch (AccessDeniedException e) {
            logBuilder.status("DENIED")
                .reason(e.getMessage());
            throw e;
        } catch (Exception e) {
            logBuilder.status("ERROR")
                .reason(e.getMessage());
            throw e;
        } finally {
            auditLogService.logAsync(logBuilder.build());
        }
    }
}
```

---

## 6. 業務層使用範例

業務層完全不包含安全邏輯程式碼，僅透過註解宣告所需權限。

### 6.1 OrderApplicationService.java

```java
@Service
@Transactional
public class OrderApplicationService {
    
    private final OrderRepository orderRepository;
    private final PaymentClient paymentClient;
    
    @RequirePermission("order:create")
    public OrderDto createOrder(CreateOrderCommand command) {
        Order order = Order.create(command);
        orderRepository.save(order);
        return OrderDto.from(order);
    }
    
    @RequirePermission("order:read")
    @RequireOwnership(
        resourceType = Order.class, 
        resourceIdParam = "orderId")
    public OrderDto getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> 
                new OrderNotFoundException(orderId));
        return OrderDto.from(order);
    }
    
    @RequirePermission({
        "order:update", 
        "payment:process"
    }, operator = LogicalOperator.AND)
    public void processPayment(
            String orderId, 
            PaymentRequest request) {
        // Feign 呼叫會自動帶上 Token
        paymentClient.processPayment(orderId, request);
    }
}
```

### 6.2 內部 API Controller

```java
@RestController
@RequestMapping("/internal/orders")
public class OrderInternalController {
    
    private final OrderQueryService queryService;
    
    @GetMapping("/{orderId}")
    @ServiceToService(allowedCallers = {
        "payment-service", 
        "inventory-service"
    })
    public OrderDto getOrderInternal(
            @PathVariable String orderId) {
        return queryService.getOrderById(orderId);
    }
}
```

---

## 7. 環境配置與測試隔離

### 7.1 Profile 配置

#### 7.1.1 application.yml

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

#### 7.1.2 application-local.yml（本地開發）

```yaml
security:
  enabled: false
  mock-user:
    id: "dev-user-001"
    roles: 
      - "ADMIN"
      - "USER"
    permissions: 
      - "order:*"
      - "payment:*"
```

#### 7.1.3 application-test.yml（測試環境）

```yaml
security:
  enabled: false
```

#### 7.1.4 application-prod.yml（生產環境）

```yaml
security:
  enabled: true

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${OAUTH2_ISSUER_URI}
```

### 7.2 Mock 安全配置

#### 7.2.1 MockSecurityConfig.java

```java
@Configuration
@Profile({"local", "test"})
public class MockSecurityConfig {
    
    @Bean
    public SecurityFilterChain mockFilterChain(
            HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> 
                auth.anyRequest().permitAll());
        return http.build();
    }
    
    @Bean
    @Primary
    public CurrentUserContext mockUserContext(
            @Value("${security.mock-user.id:test-user}") 
            String userId,
            @Value("${security.mock-user.roles:USER}") 
            Set<String> roles,
            @Value("${security.mock-user.permissions:}") 
            Set<String> permissions) {
        
        return new CurrentUserContext() {
            @Override
            public String getUserId() { 
                return userId; 
            }
            
            @Override
            public String getTenantId() { 
                return "test-tenant"; 
            }
            
            @Override
            public Set<String> getRoles() { 
                return roles; 
            }
            
            @Override
            public Set<String> getPermissions() { 
                return permissions; 
            }
            
            @Override
            public boolean hasPermission(String perm) {
                return permissions.contains(perm) ||
                    permissions.stream().anyMatch(p -> 
                        p.endsWith("*") && 
                        perm.startsWith(
                            p.substring(0, p.length()-1)));
            }
        };
    }
}
```

### 7.3 整合測試支援

#### 7.3.1 自定義測試註解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(
    factory = WithMockUserContextFactory.class)
public @interface WithMockUserContext {
    String userId() default "test-user";
    String tenantId() default "test-tenant";
    String[] roles() default {"USER"};
    String[] permissions() default {};
}
```

#### 7.3.2 WithMockUserContextFactory.java

```java
public class WithMockUserContextFactory 
    implements WithSecurityContextFactory<WithMockUserContext> {
    
    @Override
    public SecurityContext createSecurityContext(
            WithMockUserContext annotation) {
        SecurityContext context = 
            SecurityContextHolder.createEmptyContext();
        
        MockUserAuthentication auth = new MockUserAuthentication(
            annotation.userId(),
            annotation.tenantId(),
            Set.of(annotation.roles()),
            Set.of(annotation.permissions())
        );
        
        context.setAuthentication(auth);
        return context;
    }
}
```

#### 7.3.3 測試範例

```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderServiceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUserContext(
        userId = "user-123",
        permissions = {"order:create", "order:read"})
    void shouldCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"productId": "prod-1", "quantity": 2}
                """))
            .andExpect(status().isCreated());
    }
    
    @Test
    @WithMockUserContext(permissions = {})  // 無權限
    void shouldDenyWithoutPermission() throws Exception {
        mockMvc.perform(post("/api/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isForbidden());
    }
}
```

### 7.4 Feign Client 測試隔離

```java
@Configuration
@Profile("test")
public class MockFeignConfig {
    
    @Bean
    @Primary
    public PaymentClient mockPaymentClient() {
        return new PaymentClient() {
            @Override
            public PaymentResult processPayment(
                    String orderId, 
                    PaymentRequest request) {
                return PaymentResult.success("mock-payment-id");
            }
        };
    }
    
    // 或使用 WireMock
    @Bean
    public WireMockServer wireMockServer() {
        WireMockServer server = new WireMockServer(8089);
        server.start();
        
        server.stubFor(post(urlPathMatching("/internal/payments/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"status": "SUCCESS", "paymentId": "mock-123"}
                    """)));
        
        return server;
    }
}
```

---

## 8. 方案比較與選型建議

### 8.1 與 Service Mesh 比較

| 面向 | Spring AOP 方案 | Istio Service Mesh |
|------|----------------|-------------------|
| 基礎設施複雜度 | 低，純 Java | 高，需要 K8s + Sidecar |
| 語言支援 | 僅 Java/JVM | 語言無關 |
| 效能開銷 | 極低 | Sidecar 有額外延遲 |
| 測試便利性 | 容易 Mock | 需要完整環境 |
| 統一管理 | 需自行維護 | 集中控制平面 |
| mTLS 管理 | 需額外實現 | 自動管理 |
| 可觀測性 | 需額外整合 | 內建 |
| 適合規模 | < 50 服務 | 30+ 服務 |

### 8.2 選型決策樹

```
                    ┌─────────────────────────┐
                    │  服務數量 > 100?         │
                    └───────────┬─────────────┘
                           │
                ┌──────────┴──────────┐
               Yes                   No
                │                     │
                ▼                     ▼
        ┌───────────────┐    ┌─────────────────────┐
        │ 考慮 Istio    │    │ 團隊熟悉 Spring?     │
        └───────────────┘    └──────────┬──────────┘
                                   │
                        ┌──────────┴──────────┐
                       Yes                   No
                        │                     │
                        ▼                     ▼
              ┌────────────────┐    ┌───────────────┐
              │ Spring AOP 方案│    │ 評估其他方案   │
              └────────────────┘    └───────────────┘
```

### 8.3 架構總覽圖

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              生產環境                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Security Infrastructure                          │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌────────────┐ │   │
│  │  │Spring Security│ │Authorization │ │    Feign     │ │   Audit    │ │   │
│  │  │   Filters    │ │    AOP       │ │ Interceptors │ │    AOP     │ │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘ └────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      Business Services                              │   │
│  │          @RequirePermission  @RequireOwnership  @ServiceToService   │   │
│  │                    （只有註解，無安全程式碼）                          │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           開發/測試環境                                      │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                  Mock Security Infrastructure                        │   │
│  │  ┌──────────────────┐  ┌─────────────────┐  ┌────────────────────┐  │   │
│  │  │ MockUserContext  │  │ Permit All      │  │ Mock Feign Clients │  │   │
│  │  │ (可配置權限)      │  │ Security Config │  │ or WireMock        │  │   │
│  │  └──────────────────┘  └─────────────────┘  └────────────────────┘  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                    │                                        │
│                                    ▼                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      Business Services（同一份程式碼）                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 9. 附錄

### 9.1 必要相依套件

#### Maven (pom.xml)

```xml
<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- OAuth2 Resource Server -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
    
    <!-- AOP -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    
    <!-- OpenFeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- Test -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- WireMock for Feign testing -->
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>3.3.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Gradle (build.gradle)

```groovy
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
    implementation 'org.springframework.boot:spring-boot-starter-aop'
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
    
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.wiremock:wiremock-standalone:3.3.1'
}
```

### 9.2 安全檢查清單

| # | 檢查項目 | 狀態 |
|---|---------|------|
| 1 | 所有外部 API 端點都有認證保護 | ☐ |
| 2 | 敏感操作有適當的授權檢查 | ☐ |
| 3 | 服務間呼叫有身份驗證機制 | ☐ |
| 4 | Token 有適當的過期時間設定 | ☐ |
| 5 | 審計日誌記錄所有安全相關事件 | ☐ |
| 6 | 測試環境與生產環境配置隔離 | ☐ |
| 7 | 敏感配置使用環境變數或 Secret 管理 | ☐ |
| 8 | 定期輪換 Service Token | ☐ |
| 9 | 實作 Rate Limiting 防止濫用 | ☐ |
| 10 | 錯誤訊息不洩漏敏感資訊 | ☐ |

### 9.3 常見問題 FAQ

#### Q1: 如何處理 Token 過期？

```java
@Component
public class TokenCache {
    private final Cache<String, CachedToken> cache;
    
    public String getOrRefresh(String key, Supplier<String> refresher) {
        CachedToken cached = cache.getIfPresent(key);
        if (cached == null || cached.isExpiringSoon()) {
            String newToken = refresher.get();
            cache.put(key, new CachedToken(newToken));
            return newToken;
        }
        return cached.getToken();
    }
}
```

#### Q2: 如何在非同步任務中傳遞安全上下文？

```java
@Configuration
public class AsyncSecurityConfig {
    
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setTaskDecorator(new SecurityContextTaskDecorator());
        executor.initialize();
        return executor;
    }
}

public class SecurityContextTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable runnable) {
        SecurityContext context = SecurityContextHolder.getContext();
        return () -> {
            try {
                SecurityContextHolder.setContext(context);
                runnable.run();
            } finally {
                SecurityContextHolder.clearContext();
            }
        };
    }
}
```

#### Q3: 如何實現多租戶隔離？

```java
@Aspect
@Component
public class TenantIsolationAspect {
    
    private final CurrentUserContext userContext;
    
    @Before("@within(org.springframework.stereotype.Repository)")
    public void enforceTenantIsolation(JoinPoint joinPoint) {
        // 自動注入租戶過濾條件
        String tenantId = userContext.getTenantId();
        TenantContext.setCurrentTenant(tenantId);
    }
}
```

#### Q4: 如何處理服務降級時的安全考量？

```java
@Component
public class SecureFallbackFactory implements FallbackFactory<PaymentClient> {
    
    @Override
    public PaymentClient create(Throwable cause) {
        return new PaymentClient() {
            @Override
            public PaymentResult processPayment(
                    String orderId, 
                    PaymentRequest request) {
                // 記錄降級事件供後續審計
                auditLogService.logFallback(
                    "payment-service", 
                    orderId, 
                    cause.getMessage()
                );
                
                // 返回安全的降級響應
                return PaymentResult.pending(
                    "Service temporarily unavailable");
            }
        };
    }
}
```

---

*— 文件結束 —*
