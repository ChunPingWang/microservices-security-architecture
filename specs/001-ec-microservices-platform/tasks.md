# Tasks: E-Commerce Microservices Platform

**Input**: Design documents from `/specs/001-ec-microservices-platform/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: TDD Required - Each User Story must have failing tests before implementation.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Based on plan.md microservice structure:
- `security-infrastructure/` - Shared security module
- `shared-kernel/` - Shared domain objects
- `customer-service/` - Customer management
- `product-service/` - Product management
- `order-service/` - Order management
- `payment-service/` - Payment management
- `logistics-service/` - Logistics management
- `sales-service/` - Promotions and coupons
- `api-gateway/` - API Gateway
- `admin-portal/` - Admin backend

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization, shared modules, and development environment

- [x] T001 Create multi-module Gradle project structure with settings.gradle.kts
- [x] T002 [P] Configure shared-kernel module with Gradle build in shared-kernel/build.gradle.kts
- [x] T003 [P] Configure security-infrastructure module in security-infrastructure/build.gradle.kts
- [x] T004 [P] Setup Docker Compose for local development in infrastructure/docker/docker-compose.yml
- [x] T005 [P] Configure Checkstyle rules in config/checkstyle/checkstyle.xml
- [x] T006 [P] Configure SpotBugs in buildSrc for static analysis
- [x] T007 Create base domain event classes in shared-kernel/src/main/java/com/ecommerce/shared/domain/events/DomainEvent.java
- [x] T008 [P] Create shared value objects (Money, Address) in shared-kernel/src/main/java/com/ecommerce/shared/domain/value_objects/
- [x] T009 Configure Testcontainers base test in shared-kernel/src/test/java/com/ecommerce/shared/test/AbstractIntegrationTest.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core security infrastructure that MUST be complete before ANY user story

**CRITICAL**: No user story work can begin until this phase is complete

### Security Infrastructure Module

- [x] T010 Create JWT configuration in security-infrastructure/src/main/java/com/ecommerce/security/config/JwtConfig.java
- [x] T011 [P] Create JWT token provider in security-infrastructure/src/main/java/com/ecommerce/security/provider/JwtTokenProvider.java
- [x] T012 [P] Create JWT authentication filter in security-infrastructure/src/main/java/com/ecommerce/security/filter/JwtAuthenticationFilter.java
- [x] T013 Create CurrentUserContext for security context in security-infrastructure/src/main/java/com/ecommerce/security/context/CurrentUserContext.java
- [x] T014 [P] Create @RequirePermission annotation in security-infrastructure/src/main/java/com/ecommerce/security/annotation/RequirePermission.java
- [x] T015 [P] Create authorization aspect in security-infrastructure/src/main/java/com/ecommerce/security/aspect/AuthorizationAspect.java
- [x] T016 [P] Create audit logging aspect in security-infrastructure/src/main/java/com/ecommerce/security/aspect/AuditAspect.java
- [x] T017 Create Feign request interceptor for service-to-service auth in security-infrastructure/src/main/java/com/ecommerce/security/interceptor/ServiceAuthInterceptor.java
- [x] T018 Create mock security configuration for testing in security-infrastructure/src/main/java/com/ecommerce/security/mock/MockSecurityConfig.java
- [x] T019 Write unit tests for JWT token provider in security-infrastructure/src/test/java/com/ecommerce/security/provider/JwtTokenProviderTest.java

### API Gateway Foundation

- [x] T020 Create api-gateway module structure in api-gateway/build.gradle.kts
- [x] T021 Create gateway routing configuration in api-gateway/src/main/java/com/ecommerce/gateway/infrastructure/config/RoutingConfig.java
- [x] T022 [P] Create rate limiting filter in api-gateway/src/main/java/com/ecommerce/gateway/infrastructure/filter/RateLimitFilter.java
- [x] T023 [P] Create request logging filter in api-gateway/src/main/java/com/ecommerce/gateway/infrastructure/filter/RequestLoggingFilter.java
- [x] T024 Create gateway security configuration in api-gateway/src/main/java/com/ecommerce/gateway/infrastructure/config/SecurityConfig.java

### Database Foundation

- [x] T025 Create Flyway migration base structure in customer-service/src/main/resources/db/migration/
- [x] T026 [P] Configure PostgreSQL connection in customer-service/src/main/resources/application.yml
- [x] T027 [P] Configure Redis connection for session/cache in customer-service/src/main/resources/application.yml

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Customer Registration & Login (Priority: P1)

**Goal**: Enable visitors to register, login, and manage their identity

**Independent Test**: Complete registration flow, receive confirmation, login successfully, view profile page

### Tests for User Story 1 (TDD Required)

- [x] T028 [P] [US1] Contract test for POST /api/customers/register in customer-service/src/test/java/com/ecommerce/customer/contract/CustomerRegistrationContractTest.java
- [x] T029 [P] [US1] Contract test for POST /api/auth/login in customer-service/src/test/java/com/ecommerce/customer/contract/AuthenticationContractTest.java
- [x] T030 [P] [US1] Integration test for registration flow in customer-service/src/test/java/com/ecommerce/customer/integration/RegistrationFlowTest.java
- [x] T031 [P] [US1] Integration test for login and account lockout in customer-service/src/test/java/com/ecommerce/customer/integration/LoginFlowTest.java
- [x] T032 [P] [US1] Unit test for Customer aggregate in customer-service/src/test/java/com/ecommerce/customer/unit/domain/CustomerTest.java
- [x] T033 [P] [US1] Unit test for Email value object in customer-service/src/test/java/com/ecommerce/customer/unit/domain/EmailTest.java
- [x] T034 [P] [US1] Unit test for Password value object in customer-service/src/test/java/com/ecommerce/customer/unit/domain/PasswordTest.java

### Implementation for User Story 1

- [x] T035 [P] [US1] Create Email value object in customer-service/src/main/java/com/ecommerce/customer/domain/value_objects/Email.java
- [x] T036 [P] [US1] Create Password value object with BCrypt hashing in customer-service/src/main/java/com/ecommerce/customer/domain/value_objects/Password.java
- [x] T037 [P] [US1] Create MemberLevel enum in customer-service/src/main/java/com/ecommerce/customer/domain/value_objects/MemberLevel.java
- [x] T038 [US1] Create Customer entity in customer-service/src/main/java/com/ecommerce/customer/domain/entities/Customer.java
- [x] T039 [P] [US1] Create Address entity in customer-service/src/main/java/com/ecommerce/customer/domain/entities/Address.java
- [x] T040 [US1] CustomerAggregate - Customer entity already includes aggregate root behavior
- [x] T041 [P] [US1] Create CustomerRegistered event in customer-service/src/main/java/com/ecommerce/customer/domain/events/CustomerRegistered.java
- [x] T042 [US1] Create CustomerRepository port in customer-service/src/main/java/com/ecommerce/customer/domain/ports/CustomerRepository.java
- [x] T043 [US1] Create RegisterCustomerUseCase in customer-service/src/main/java/com/ecommerce/customer/application/usecases/RegisterCustomerUseCase.java
- [x] T044 [US1] Create AuthenticateCustomerUseCase in customer-service/src/main/java/com/ecommerce/customer/application/usecases/AuthenticateCustomerUseCase.java
- [x] T045 [US1] Create JPA Customer entity in customer-service/src/main/java/com/ecommerce/customer/infrastructure/persistence/entities/CustomerJpaEntity.java
- [x] T046 [US1] Create JPA CustomerRepository implementation in customer-service/src/main/java/com/ecommerce/customer/infrastructure/persistence/adapters/CustomerRepositoryAdapter.java
- [x] T047 [US1] Create CustomerController REST API in customer-service/src/main/java/com/ecommerce/customer/infrastructure/web/controllers/CustomerController.java
- [x] T048 [US1] Create AuthController REST API in customer-service/src/main/java/com/ecommerce/customer/infrastructure/web/controllers/AuthController.java
- [x] T049 [US1] Create Flyway migration V1__create_customer_tables.sql in customer-service/src/main/resources/db/migration/V1__create_customers_tables.sql
- [x] T050 [US1] Add account lockout logic after 5 failed attempts in AuthenticateCustomerUseCase

**Checkpoint**: User Story 1 fully functional - customers can register and login

---

## Phase 4: User Story 2 - Product Browse & Search (Priority: P1)

**Goal**: Enable customers to browse product catalog and search for products

**Independent Test**: Browse categories, search by keyword, view product details with stock status

### Tests for User Story 2 (TDD Required)

- [ ] T051 [P] [US2] Contract test for GET /api/products in product-service/src/test/java/com/ecommerce/product/contract/ProductListContractTest.java
- [ ] T052 [P] [US2] Contract test for GET /api/products/search in product-service/src/test/java/com/ecommerce/product/contract/ProductSearchContractTest.java
- [ ] T053 [P] [US2] Contract test for GET /api/products/{id} in product-service/src/test/java/com/ecommerce/product/contract/ProductDetailContractTest.java
- [ ] T054 [P] [US2] Integration test for category browsing in product-service/src/test/java/com/ecommerce/product/integration/CategoryBrowseTest.java
- [ ] T055 [P] [US2] Integration test for Elasticsearch search in product-service/src/test/java/com/ecommerce/product/integration/ProductSearchTest.java
- [ ] T056 [P] [US2] Unit test for Product aggregate in product-service/src/test/java/com/ecommerce/product/unit/domain/ProductTest.java
- [ ] T057 [P] [US2] Unit test for SKU value object in product-service/src/test/java/com/ecommerce/product/unit/domain/SKUTest.java

### Implementation for User Story 2

- [ ] T058 [P] [US2] Create SKU value object in product-service/src/main/java/com/ecommerce/product/domain/value_objects/SKU.java
- [ ] T059 [P] [US2] Create Price value object in product-service/src/main/java/com/ecommerce/product/domain/value_objects/Price.java
- [ ] T060 [P] [US2] Create Stock value object in product-service/src/main/java/com/ecommerce/product/domain/value_objects/Stock.java
- [ ] T061 [US2] Create Product entity in product-service/src/main/java/com/ecommerce/product/domain/entities/Product.java
- [ ] T062 [P] [US2] Create Category entity in product-service/src/main/java/com/ecommerce/product/domain/entities/Category.java
- [ ] T063 [P] [US2] Create Inventory entity in product-service/src/main/java/com/ecommerce/product/domain/entities/Inventory.java
- [ ] T064 [US2] Create ProductAggregate in product-service/src/main/java/com/ecommerce/product/domain/aggregates/ProductAggregate.java
- [ ] T065 [US2] Create ProductRepository port in product-service/src/main/java/com/ecommerce/product/domain/ports/ProductRepository.java
- [ ] T066 [US2] Create ProductSearchPort for search abstraction in product-service/src/main/java/com/ecommerce/product/domain/ports/ProductSearchPort.java
- [ ] T067 [US2] Create BrowseProductsUseCase in product-service/src/main/java/com/ecommerce/product/application/use_cases/BrowseProductsUseCase.java
- [ ] T068 [US2] Create SearchProductsUseCase in product-service/src/main/java/com/ecommerce/product/application/use_cases/SearchProductsUseCase.java
- [ ] T069 [US2] Create GetProductDetailUseCase in product-service/src/main/java/com/ecommerce/product/application/use_cases/GetProductDetailUseCase.java
- [ ] T070 [US2] Create JPA Product entity in product-service/src/main/java/com/ecommerce/product/infrastructure/persistence/JpaProductEntity.java
- [ ] T071 [US2] Create JPA ProductRepository implementation in product-service/src/main/java/com/ecommerce/product/infrastructure/persistence/JpaProductRepository.java
- [ ] T072 [US2] Create Elasticsearch adapter in product-service/src/main/java/com/ecommerce/product/infrastructure/search/ElasticsearchProductAdapter.java
- [ ] T073 [US2] Create ProductController REST API in product-service/src/main/java/com/ecommerce/product/infrastructure/web/ProductController.java
- [ ] T074 [US2] Create CategoryController REST API in product-service/src/main/java/com/ecommerce/product/infrastructure/web/CategoryController.java
- [ ] T075 [US2] Create Flyway migration V1__create_product_tables.sql in product-service/src/main/resources/db/migration/V1__create_product_tables.sql
- [ ] T076 [US2] Configure Elasticsearch in product-service/src/main/resources/application.yml

**Checkpoint**: User Story 2 fully functional - customers can browse and search products

---

## Phase 5: User Story 3 - Shopping Cart Management (Priority: P1)

**Goal**: Enable customers to manage shopping cart with multiple products

**Independent Test**: Add product, modify quantity, remove product, view cart total

### Tests for User Story 3 (TDD Required)

- [ ] T077 [P] [US3] Contract test for POST /api/cart/items in order-service/src/test/java/com/ecommerce/order/contract/AddToCartContractTest.java
- [ ] T078 [P] [US3] Contract test for PUT /api/cart/items/{id} in order-service/src/test/java/com/ecommerce/order/contract/UpdateCartItemContractTest.java
- [ ] T079 [P] [US3] Contract test for DELETE /api/cart/items/{id} in order-service/src/test/java/com/ecommerce/order/contract/RemoveCartItemContractTest.java
- [ ] T080 [P] [US3] Contract test for GET /api/cart in order-service/src/test/java/com/ecommerce/order/contract/GetCartContractTest.java
- [ ] T081 [P] [US3] Integration test for cart management flow in order-service/src/test/java/com/ecommerce/order/integration/CartManagementFlowTest.java
- [ ] T082 [P] [US3] Unit test for Cart aggregate in order-service/src/test/java/com/ecommerce/order/unit/domain/CartTest.java

### Implementation for User Story 3

- [ ] T083 [P] [US3] Create CartItem entity in order-service/src/main/java/com/ecommerce/order/domain/entities/CartItem.java
- [ ] T084 [US3] Create Cart aggregate in order-service/src/main/java/com/ecommerce/order/domain/aggregates/Cart.java
- [ ] T085 [US3] Create CartRepository port in order-service/src/main/java/com/ecommerce/order/domain/ports/CartRepository.java
- [ ] T086 [US3] Create ProductServicePort for stock validation in order-service/src/main/java/com/ecommerce/order/domain/ports/ProductServicePort.java
- [ ] T087 [US3] Create AddToCartUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/AddToCartUseCase.java
- [ ] T088 [US3] Create UpdateCartItemUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/UpdateCartItemUseCase.java
- [ ] T089 [US3] Create RemoveCartItemUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/RemoveCartItemUseCase.java
- [ ] T090 [US3] Create GetCartUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/GetCartUseCase.java
- [ ] T091 [US3] Create Redis Cart repository in order-service/src/main/java/com/ecommerce/order/infrastructure/persistence/RedisCartRepository.java
- [ ] T092 [US3] Create ProductServiceClient (Feign) in order-service/src/main/java/com/ecommerce/order/infrastructure/adapter/ProductServiceClient.java
- [ ] T093 [US3] Create CartController REST API in order-service/src/main/java/com/ecommerce/order/infrastructure/web/CartController.java
- [ ] T094 [US3] Configure Redis for cart storage in order-service/src/main/resources/application.yml

**Checkpoint**: User Story 3 fully functional - customers can manage shopping cart

---

## Phase 6: User Story 4 - Order Creation & Payment (Priority: P1)

**Goal**: Enable customers to checkout and complete payment

**Independent Test**: Create order from cart, select payment method, complete payment, receive confirmation

### Tests for User Story 4 (TDD Required)

- [ ] T095 [P] [US4] Contract test for POST /api/orders in order-service/src/test/java/com/ecommerce/order/contract/CreateOrderContractTest.java
- [ ] T096 [P] [US4] Contract test for POST /api/payments in payment-service/src/test/java/com/ecommerce/payment/contract/ProcessPaymentContractTest.java
- [ ] T097 [P] [US4] Integration test for checkout flow in order-service/src/test/java/com/ecommerce/order/integration/CheckoutFlowTest.java
- [ ] T098 [P] [US4] Integration test for payment timeout in order-service/src/test/java/com/ecommerce/order/integration/PaymentTimeoutTest.java
- [ ] T099 [P] [US4] Unit test for Order aggregate in order-service/src/test/java/com/ecommerce/order/unit/domain/OrderTest.java
- [ ] T100 [P] [US4] Unit test for Payment aggregate in payment-service/src/test/java/com/ecommerce/payment/unit/domain/PaymentTest.java

### Implementation for User Story 4 - Order Service

- [ ] T101 [P] [US4] Create OrderId value object in order-service/src/main/java/com/ecommerce/order/domain/value_objects/OrderId.java
- [ ] T102 [P] [US4] Create OrderStatus enum in order-service/src/main/java/com/ecommerce/order/domain/value_objects/OrderStatus.java
- [ ] T103 [US4] Create Order entity in order-service/src/main/java/com/ecommerce/order/domain/entities/Order.java
- [ ] T104 [P] [US4] Create OrderItem entity in order-service/src/main/java/com/ecommerce/order/domain/entities/OrderItem.java
- [ ] T105 [US4] Create OrderAggregate in order-service/src/main/java/com/ecommerce/order/domain/aggregates/OrderAggregate.java
- [ ] T106 [P] [US4] Create OrderCreated event in order-service/src/main/java/com/ecommerce/order/domain/events/OrderCreated.java
- [ ] T107 [P] [US4] Create OrderPaid event in order-service/src/main/java/com/ecommerce/order/domain/events/OrderPaid.java
- [ ] T108 [P] [US4] Create OrderCancelled event in order-service/src/main/java/com/ecommerce/order/domain/events/OrderCancelled.java
- [ ] T109 [US4] Create OrderRepository port in order-service/src/main/java/com/ecommerce/order/domain/ports/OrderRepository.java
- [ ] T110 [US4] Create CreateOrderUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/CreateOrderUseCase.java
- [ ] T111 [US4] Create CancelOrderUseCase in order-service/src/main/java/com/ecommerce/order/application/use_cases/CancelOrderUseCase.java
- [ ] T112 [US4] Create JPA Order entity in order-service/src/main/java/com/ecommerce/order/infrastructure/persistence/JpaOrderEntity.java
- [ ] T113 [US4] Create JPA OrderRepository implementation in order-service/src/main/java/com/ecommerce/order/infrastructure/persistence/JpaOrderRepository.java
- [ ] T114 [US4] Create OrderController REST API in order-service/src/main/java/com/ecommerce/order/infrastructure/web/OrderController.java
- [ ] T115 [US4] Create order timeout scheduler in order-service/src/main/java/com/ecommerce/order/infrastructure/scheduler/OrderTimeoutScheduler.java
- [ ] T116 [US4] Create Flyway migration V1__create_order_tables.sql in order-service/src/main/resources/db/migration/V1__create_order_tables.sql

### Implementation for User Story 4 - Payment Service

- [ ] T117 [P] [US4] Create PaymentMethod enum in payment-service/src/main/java/com/ecommerce/payment/domain/value_objects/PaymentMethod.java
- [ ] T118 [P] [US4] Create TransactionId value object in payment-service/src/main/java/com/ecommerce/payment/domain/value_objects/TransactionId.java
- [ ] T119 [US4] Create Payment entity in payment-service/src/main/java/com/ecommerce/payment/domain/entities/Payment.java
- [ ] T120 [P] [US4] Create Refund entity in payment-service/src/main/java/com/ecommerce/payment/domain/entities/Refund.java
- [ ] T121 [US4] Create PaymentAggregate in payment-service/src/main/java/com/ecommerce/payment/domain/aggregates/PaymentAggregate.java
- [ ] T122 [P] [US4] Create PaymentCompleted event in payment-service/src/main/java/com/ecommerce/payment/domain/events/PaymentCompleted.java
- [ ] T123 [P] [US4] Create RefundInitiated event in payment-service/src/main/java/com/ecommerce/payment/domain/events/RefundInitiated.java
- [ ] T124 [US4] Create PaymentGatewayPort for payment abstraction in payment-service/src/main/java/com/ecommerce/payment/domain/ports/PaymentGatewayPort.java
- [ ] T125 [US4] Create PaymentRepository port in payment-service/src/main/java/com/ecommerce/payment/domain/ports/PaymentRepository.java
- [ ] T126 [US4] Create ProcessPaymentUseCase in payment-service/src/main/java/com/ecommerce/payment/application/use_cases/ProcessPaymentUseCase.java
- [ ] T127 [US4] Create RefundPaymentUseCase in payment-service/src/main/java/com/ecommerce/payment/application/use_cases/RefundPaymentUseCase.java
- [ ] T128 [US4] Create mock payment gateway adapter in payment-service/src/main/java/com/ecommerce/payment/infrastructure/gateway/MockPaymentGatewayAdapter.java
- [ ] T129 [US4] Create JPA Payment entity in payment-service/src/main/java/com/ecommerce/payment/infrastructure/persistence/JpaPaymentEntity.java
- [ ] T130 [US4] Create JPA PaymentRepository implementation in payment-service/src/main/java/com/ecommerce/payment/infrastructure/persistence/JpaPaymentRepository.java
- [ ] T131 [US4] Create PaymentController REST API in payment-service/src/main/java/com/ecommerce/payment/infrastructure/web/PaymentController.java
- [ ] T132 [US4] Create Flyway migration V1__create_payment_tables.sql in payment-service/src/main/resources/db/migration/V1__create_payment_tables.sql
- [ ] T133 [US4] Create AES-256 encryption utility for card data in payment-service/src/main/java/com/ecommerce/payment/infrastructure/security/CardDataEncryptor.java

**Checkpoint**: User Story 4 fully functional - complete checkout and payment flow (MVP Complete!)

---

## Phase 7: User Story 5 - Order Tracking & Logistics (Priority: P2)

**Goal**: Enable customers to track order status and logistics progress

**Independent Test**: Query order status, view logistics tracking, receive status notifications

### Tests for User Story 5 (TDD Required)

- [ ] T134 [P] [US5] Contract test for GET /api/orders in order-service/src/test/java/com/ecommerce/order/contract/GetOrdersContractTest.java
- [ ] T135 [P] [US5] Contract test for GET /api/shipments/{orderId}/tracking in logistics-service/src/test/java/com/ecommerce/logistics/contract/TrackingContractTest.java
- [ ] T136 [P] [US5] Integration test for order tracking flow in order-service/src/test/java/com/ecommerce/order/integration/OrderTrackingFlowTest.java
- [ ] T137 [P] [US5] Unit test for Shipment aggregate in logistics-service/src/test/java/com/ecommerce/logistics/unit/domain/ShipmentTest.java

### Implementation for User Story 5

- [ ] T138 [P] [US5] Create TrackingNumber value object in logistics-service/src/main/java/com/ecommerce/logistics/domain/value_objects/TrackingNumber.java
- [ ] T139 [P] [US5] Create Carrier enum in logistics-service/src/main/java/com/ecommerce/logistics/domain/value_objects/Carrier.java
- [ ] T140 [P] [US5] Create DeliveryStatus enum in logistics-service/src/main/java/com/ecommerce/logistics/domain/value_objects/DeliveryStatus.java
- [ ] T141 [US5] Create Shipment entity in logistics-service/src/main/java/com/ecommerce/logistics/domain/entities/Shipment.java
- [ ] T142 [US5] Create ShipmentAggregate in logistics-service/src/main/java/com/ecommerce/logistics/domain/aggregates/ShipmentAggregate.java
- [ ] T143 [P] [US5] Create ShipmentCreated event in logistics-service/src/main/java/com/ecommerce/logistics/domain/events/ShipmentCreated.java
- [ ] T144 [P] [US5] Create StatusUpdated event in logistics-service/src/main/java/com/ecommerce/logistics/domain/events/StatusUpdated.java
- [ ] T145 [US5] Create LogisticsProviderPort in logistics-service/src/main/java/com/ecommerce/logistics/domain/ports/LogisticsProviderPort.java
- [ ] T146 [US5] Create ShipmentRepository port in logistics-service/src/main/java/com/ecommerce/logistics/domain/ports/ShipmentRepository.java
- [ ] T147 [US5] Create CreateShipmentUseCase in logistics-service/src/main/java/com/ecommerce/logistics/application/use_cases/CreateShipmentUseCase.java
- [ ] T148 [US5] Create TrackShipmentUseCase in logistics-service/src/main/java/com/ecommerce/logistics/application/use_cases/TrackShipmentUseCase.java
- [ ] T149 [US5] Create mock logistics provider adapter in logistics-service/src/main/java/com/ecommerce/logistics/infrastructure/provider/MockLogisticsAdapter.java
- [ ] T150 [US5] Create JPA Shipment entity in logistics-service/src/main/java/com/ecommerce/logistics/infrastructure/persistence/JpaShipmentEntity.java
- [ ] T151 [US5] Create JPA ShipmentRepository implementation in logistics-service/src/main/java/com/ecommerce/logistics/infrastructure/persistence/JpaShipmentRepository.java
- [ ] T152 [US5] Create ShipmentController REST API in logistics-service/src/main/java/com/ecommerce/logistics/infrastructure/web/ShipmentController.java
- [ ] T153 [US5] Create Flyway migration V1__create_shipment_tables.sql in logistics-service/src/main/resources/db/migration/V1__create_shipment_tables.sql
- [ ] T154 [US5] Extend OrderController with order history endpoint in order-service/src/main/java/com/ecommerce/order/infrastructure/web/OrderController.java
- [ ] T155 [US5] Create notification service for status updates in logistics-service/src/main/java/com/ecommerce/logistics/application/services/NotificationService.java

**Checkpoint**: User Story 5 fully functional - order tracking and logistics available

---

## Phase 8: User Story 6 - Promotions & Coupons (Priority: P2)

**Goal**: Enable customers to use coupons and participate in promotions

**Independent Test**: Apply coupon code, see discount applied, view promotional products

### Tests for User Story 6 (TDD Required)

- [ ] T156 [P] [US6] Contract test for POST /api/cart/coupon in sales-service/src/test/java/com/ecommerce/sales/contract/ApplyCouponContractTest.java
- [ ] T157 [P] [US6] Contract test for GET /api/promotions in sales-service/src/test/java/com/ecommerce/sales/contract/GetPromotionsContractTest.java
- [ ] T158 [P] [US6] Integration test for coupon application in sales-service/src/test/java/com/ecommerce/sales/integration/CouponApplicationTest.java
- [ ] T159 [P] [US6] Unit test for Promotion aggregate in sales-service/src/test/java/com/ecommerce/sales/unit/domain/PromotionTest.java
- [ ] T160 [P] [US6] Unit test for Coupon entity in sales-service/src/test/java/com/ecommerce/sales/unit/domain/CouponTest.java

### Implementation for User Story 6

- [ ] T161 [P] [US6] Create DiscountRule value object in sales-service/src/main/java/com/ecommerce/sales/domain/value_objects/DiscountRule.java
- [ ] T162 [P] [US6] Create CouponCode value object in sales-service/src/main/java/com/ecommerce/sales/domain/value_objects/CouponCode.java
- [ ] T163 [US6] Create Promotion entity in sales-service/src/main/java/com/ecommerce/sales/domain/entities/Promotion.java
- [ ] T164 [US6] Create Coupon entity in sales-service/src/main/java/com/ecommerce/sales/domain/entities/Coupon.java
- [ ] T165 [US6] Create PromotionAggregate in sales-service/src/main/java/com/ecommerce/sales/domain/aggregates/PromotionAggregate.java
- [ ] T166 [P] [US6] Create CouponUsed event in sales-service/src/main/java/com/ecommerce/sales/domain/events/CouponUsed.java
- [ ] T167 [P] [US6] Create PromotionStarted event in sales-service/src/main/java/com/ecommerce/sales/domain/events/PromotionStarted.java
- [ ] T168 [US6] Create PromotionRepository port in sales-service/src/main/java/com/ecommerce/sales/domain/ports/PromotionRepository.java
- [ ] T169 [US6] Create CouponRepository port in sales-service/src/main/java/com/ecommerce/sales/domain/ports/CouponRepository.java
- [ ] T170 [US6] Create ApplyCouponUseCase in sales-service/src/main/java/com/ecommerce/sales/application/use_cases/ApplyCouponUseCase.java
- [ ] T171 [US6] Create GetPromotionsUseCase in sales-service/src/main/java/com/ecommerce/sales/application/use_cases/GetPromotionsUseCase.java
- [ ] T172 [US6] Create JPA Promotion entity in sales-service/src/main/java/com/ecommerce/sales/infrastructure/persistence/JpaPromotionEntity.java
- [ ] T173 [US6] Create JPA Coupon entity in sales-service/src/main/java/com/ecommerce/sales/infrastructure/persistence/JpaCouponEntity.java
- [ ] T174 [US6] Create JPA repository implementations in sales-service/src/main/java/com/ecommerce/sales/infrastructure/persistence/
- [ ] T175 [US6] Create PromotionController REST API in sales-service/src/main/java/com/ecommerce/sales/infrastructure/web/PromotionController.java
- [ ] T176 [US6] Create CouponController REST API in sales-service/src/main/java/com/ecommerce/sales/infrastructure/web/CouponController.java
- [ ] T177 [US6] Create Flyway migration V1__create_sales_tables.sql in sales-service/src/main/resources/db/migration/V1__create_sales_tables.sql
- [ ] T178 [US6] Integrate coupon validation with cart checkout in order-service/src/main/java/com/ecommerce/order/application/use_cases/CreateOrderUseCase.java

**Checkpoint**: User Story 6 fully functional - promotions and coupons working

---

## Phase 9: User Story 7 - Member Levels & Benefits (Priority: P3)

**Goal**: Enable customers to earn member levels and enjoy benefits

**Independent Test**: View member level, see level upgrade after purchase, access level-specific benefits

### Tests for User Story 7 (TDD Required)

- [ ] T179 [P] [US7] Contract test for GET /api/customers/me/membership in customer-service/src/test/java/com/ecommerce/customer/contract/MembershipContractTest.java
- [ ] T180 [P] [US7] Integration test for level upgrade flow in customer-service/src/test/java/com/ecommerce/customer/integration/MemberLevelUpgradeTest.java
- [ ] T181 [P] [US7] Unit test for member level calculation in customer-service/src/test/java/com/ecommerce/customer/unit/domain/MemberLevelCalculatorTest.java

### Implementation for User Story 7

- [ ] T182 [US7] Create MemberLevelCalculator domain service in customer-service/src/main/java/com/ecommerce/customer/domain/services/MemberLevelCalculator.java
- [ ] T183 [P] [US7] Create LevelUpgraded event in customer-service/src/main/java/com/ecommerce/customer/domain/events/LevelUpgraded.java
- [ ] T184 [US7] Create GetMembershipUseCase in customer-service/src/main/java/com/ecommerce/customer/application/use_cases/GetMembershipUseCase.java
- [ ] T185 [US7] Create UpdateMemberLevelUseCase in customer-service/src/main/java/com/ecommerce/customer/application/use_cases/UpdateMemberLevelUseCase.java
- [ ] T186 [US7] Create event handler for OrderPaid to update spending in customer-service/src/main/java/com/ecommerce/customer/infrastructure/messaging/OrderPaidEventHandler.java
- [ ] T187 [US7] Extend CustomerController with membership endpoint in customer-service/src/main/java/com/ecommerce/customer/infrastructure/web/CustomerController.java
- [ ] T188 [US7] Create Flyway migration V2__add_member_level_columns.sql in customer-service/src/main/resources/db/migration/V2__add_member_level_columns.sql

**Checkpoint**: User Story 7 fully functional - member levels working

---

## Phase 10: Admin Portal (Cross-Cutting)

**Goal**: Provide backend management capabilities for administrators

### Tests for Admin Portal (TDD Required)

- [ ] T189 [P] Contract test for admin authentication in admin-portal/backend/src/test/java/com/ecommerce/admin/contract/AdminAuthContractTest.java
- [ ] T190 [P] Contract test for product management APIs in admin-portal/backend/src/test/java/com/ecommerce/admin/contract/ProductManagementContractTest.java
- [ ] T191 [P] Integration test for admin operations in admin-portal/backend/src/test/java/com/ecommerce/admin/integration/AdminOperationsTest.java

### Implementation for Admin Portal

- [ ] T192 Create Admin entity in admin-portal/backend/src/main/java/com/ecommerce/admin/domain/entities/Admin.java
- [ ] T193 [P] Create AdminRole enum in admin-portal/backend/src/main/java/com/ecommerce/admin/domain/value_objects/AdminRole.java
- [ ] T194 Create AdminRepository port in admin-portal/backend/src/main/java/com/ecommerce/admin/domain/ports/AdminRepository.java
- [ ] T195 Create AdminAuthUseCase in admin-portal/backend/src/main/java/com/ecommerce/admin/application/use_cases/AdminAuthUseCase.java
- [ ] T196 Create ProductManagementController in admin-portal/backend/src/main/java/com/ecommerce/admin/infrastructure/web/ProductManagementController.java
- [ ] T197 Create OrderManagementController in admin-portal/backend/src/main/java/com/ecommerce/admin/infrastructure/web/OrderManagementController.java
- [ ] T198 Create PromotionManagementController in admin-portal/backend/src/main/java/com/ecommerce/admin/infrastructure/web/PromotionManagementController.java
- [ ] T199 Create CustomerManagementController in admin-portal/backend/src/main/java/com/ecommerce/admin/infrastructure/web/CustomerManagementController.java
- [ ] T200 Create ReportController for sales reports in admin-portal/backend/src/main/java/com/ecommerce/admin/infrastructure/web/ReportController.java
- [ ] T201 Create Flyway migration V1__create_admin_tables.sql in admin-portal/backend/src/main/resources/db/migration/V1__create_admin_tables.sql

**Checkpoint**: Admin Portal functional - complete back-office management

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements affecting multiple user stories

- [ ] T202 [P] Create API documentation with OpenAPI in api-gateway/src/main/resources/openapi/
- [ ] T203 [P] Configure Micrometer metrics for all services
- [ ] T204 [P] Configure distributed tracing with Sleuth/Zipkin
- [ ] T205 [P] Create health check endpoints for all services
- [ ] T206 Security audit for PCI DSS compliance
- [ ] T207 Performance testing and optimization
- [ ] T208 [P] Create Kubernetes manifests in infrastructure/k8s/manifests/
- [ ] T209 Final integration testing across all services
- [ ] T210 Documentation review and updates

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup - BLOCKS all user stories
- **User Stories (Phase 3-9)**: All depend on Foundational phase completion
  - P1 stories (US1-4) can proceed in priority order
  - P2 stories (US5-6) depend on P1 completion for data flow
  - P3 stories (US7) depend on US1 and US4 for customer and order data
- **Admin Portal (Phase 10)**: Can start after US1-4 are complete
- **Polish (Phase 11)**: Depends on all desired user stories being complete

### User Story Dependencies

| User Story | Depends On | Can Start After |
|------------|------------|-----------------|
| US1 (Customer Auth) | Phase 2 | Foundation complete |
| US2 (Product Browse) | Phase 2 | Foundation complete |
| US3 (Shopping Cart) | US2 | Product service ready |
| US4 (Order & Payment) | US1, US3 | Auth and cart ready |
| US5 (Order Tracking) | US4 | Order flow complete |
| US6 (Promotions) | US4 | Order flow complete |
| US7 (Member Levels) | US1, US4 | Customer and order ready |

### Within Each User Story

1. Tests MUST be written and FAIL before implementation (TDD)
2. Value objects before entities
3. Entities before aggregates
4. Domain ports before application use cases
5. Use cases before infrastructure adapters
6. Infrastructure before controllers
7. Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel
- Tests within a story marked [P] can run in parallel
- Models within a story marked [P] can run in parallel
- US1 and US2 can be developed in parallel by separate teams

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: T028 "Contract test for registration"
Task: T029 "Contract test for login"
Task: T030 "Integration test for registration flow"
Task: T031 "Integration test for login flow"
Task: T032-T034 "Unit tests for domain objects"

# Launch all value objects together:
Task: T035 "Create Email value object"
Task: T036 "Create Password value object"
Task: T037 "Create MemberLevel enum"
```

---

## Implementation Strategy

### MVP First (User Stories 1-4 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL)
3. Complete Phase 3: User Story 1 (Customer Auth)
4. Complete Phase 4: User Story 2 (Product Browse)
5. Complete Phase 5: User Story 3 (Shopping Cart)
6. Complete Phase 6: User Story 4 (Order & Payment)
7. **STOP and VALIDATE**: Test complete purchase flow
8. Deploy/demo if ready - **This is MVP!**

### Incremental Delivery

1. Setup + Foundation → Foundation ready
2. US1 → Test → Deploy (Auth working)
3. US2 → Test → Deploy (Catalog browsable)
4. US3 → Test → Deploy (Cart functional)
5. US4 → Test → Deploy **(MVP - Complete purchase flow!)**
6. US5 → Test → Deploy (Order tracking added)
7. US6 → Test → Deploy (Promotions added)
8. US7 → Test → Deploy (Member levels added)
9. Admin Portal → Test → Deploy (Back-office ready)
10. Polish → Final release

---

## Summary

| Category | Count |
|----------|-------|
| **Total Tasks** | 210 |
| Phase 1 - Setup | 9 |
| Phase 2 - Foundational | 18 |
| US1 - Customer Auth | 23 |
| US2 - Product Browse | 26 |
| US3 - Shopping Cart | 18 |
| US4 - Order & Payment | 39 |
| US5 - Order Tracking | 22 |
| US6 - Promotions | 23 |
| US7 - Member Levels | 10 |
| Phase 10 - Admin Portal | 13 |
| Phase 11 - Polish | 9 |

**MVP Scope**: Phases 1-6 (US1-US4) = 133 tasks
**Full Scope**: All phases = 210 tasks

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **TDD**: Write failing tests (red), then implement (green), then refactor
- **Commit**: Commit after each task completion (Traditional Chinese message)
- Stop at any checkpoint to validate story independently
- Avoid: vague tasks, same file conflicts, cross-story dependencies
