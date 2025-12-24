# Implementation Plan: 電商微服務系統平台

**Branch**: `001-ec-microservices-platform` | **Date**: 2025-12-23 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-ec-microservices-platform/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

建構一套基於 DDD 六角形架構的電商微服務平台，採用 Spring Security 實現南北向（外部請求）與東西向（服務間）的安全認證與授權機制。系統包含六大核心服務：客戶管理、商品管理、訂單管理、付款管理、物流管理、銷售管理，並搭配完整的後台管理系統。技術採用 Java 21 + Spring Boot 3.x，遵循 OAuth 2.0/OIDC 標準，實現 PCI DSS 合規的安全架構。

## Technical Context

**Language/Version**: Java 21 (LTS), Spring Boot 3.2.x
**Primary Dependencies**: Spring Security 6.x, Spring Data JPA, Spring Cloud OpenFeign, Spring AOP
**Storage**: PostgreSQL 15+ (主要資料庫), Redis 7+ (快取/Session), Elasticsearch 8+ (商品搜尋)
**Testing**: JUnit 5, Testcontainers, WireMock, Spring Security Test
**Target Platform**: Linux (Docker/Kubernetes), AWS/GCP/Azure Cloud
**Project Type**: 微服務（多專案結構）
**Performance Goals**: API 回應 p95 < 200ms, 商品搜尋 < 1s, 支援 10,000 同時在線用戶
**Constraints**: 99.99% 可用性, PCI DSS 合規, 信用卡資訊 AES-256 加密, TLS 1.2+
**Scale/Scope**: 6 核心微服務 + 1 API Gateway + 1 後台管理系統

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原則 | 檢查項目 | 狀態 |
|------|----------|------|
| **TDD** | 測試先於實作撰寫？測試案例已確認失敗？ | ✅ 規劃採用 TDD 流程 |
| **BDD** | User Story 包含 Gherkin 情境？情境可獨立驗證？ | ✅ spec.md 已包含 7 個 User Story 的 BDD 情境 |
| **DDD** | Domain Model 已識別？使用 Ubiquitous Language？ | ✅ 已識別 7 個核心 Entity（Customer, Product, Order, Payment, Shipment, Promotion, Admin） |
| **SOLID** | 設計遵循 SOLID 原則？依賴抽象而非具體？ | ✅ 透過六角形架構 Port/Adapter 實現依賴反轉 |
| **六角形架構** | Domain 層無框架依賴？依賴方向由外向內？ | ✅ 參考 spring-security-microservices-architecture.md 設計 |
| **程式碼品質** | 函式 ≤30行？類別 ≤300行？巢狀 ≤4層？ | ✅ 將於實作時遵循 |
| **測試標準** | 測試金字塔分層？Unit/Integration/E2E？ | ✅ 規劃 Unit/Integration/Contract/E2E 測試 |
| **提交紀律** | 每 Task 一次提交？訊息使用繁體中文？ | ✅ 將於實作時遵循 |

**架構約束**：
- [x] 框架依賴限於 `infrastructure/` 目錄
- [x] Repository 介面定義於 `domain/ports/`
- [x] 安全邏輯抽象於 Domain Service（參考 security-infrastructure 模組）

## Project Structure

### Documentation (this feature)

```text
specs/001-ec-microservices-platform/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
# 微服務多專案結構

security-infrastructure/          # 獨立安全基礎設施模組（可被所有服務依賴）
├── src/main/java/
│   └── com/example/security/
│       ├── config/               # Spring Security 配置
│       ├── filter/               # 自定義 Filter
│       ├── aspect/               # AOP 切面（授權、審計）
│       ├── interceptor/          # Feign/RestTemplate 攔截器
│       ├── context/              # 安全上下文（CurrentUserContext）
│       ├── annotation/           # 自定義註解（@RequirePermission 等）
│       └── mock/                 # 測試用 Mock 配置
└── src/test/java/

api-gateway/                      # API Gateway 服務
├── src/main/java/
│   └── com/ecommerce/gateway/
│       ├── domain/               # Gateway Domain（路由規則等）
│       │   ├── entities/
│       │   └── ports/
│       ├── application/          # Use Cases
│       └── infrastructure/       # Spring Cloud Gateway 配置
└── src/test/java/

customer-service/                 # 客戶管理服務
├── src/main/java/
│   └── com/ecommerce/customer/
│       ├── domain/               # 純業務邏輯，無框架依賴
│       │   ├── entities/         # Customer, Address
│       │   ├── value_objects/    # Email, Password, MemberLevel
│       │   ├── aggregates/       # CustomerAggregate
│       │   ├── events/           # CustomerRegistered, LevelUpgraded
│       │   ├── services/         # Domain Services
│       │   └── ports/            # Repository interfaces
│       ├── application/          # Use Cases / Application Services
│       │   ├── use_cases/        # RegisterCustomer, AuthenticateCustomer
│       │   └── services/         # Application Services
│       └── infrastructure/       # 框架整合
│           ├── adapters/         # Port 實作
│           ├── persistence/      # JPA Repository 實作
│           ├── web/              # REST Controllers
│           └── config/           # Spring 配置
└── src/test/java/
    ├── unit/                     # Domain 層單元測試
    ├── integration/              # 整合測試
    └── contract/                 # API 契約測試

product-service/                  # 商品管理服務
├── src/main/java/
│   └── com/ecommerce/product/
│       ├── domain/
│       │   ├── entities/         # Product, Category, Inventory
│       │   ├── value_objects/    # SKU, Price, Stock
│       │   ├── aggregates/       # ProductAggregate
│       │   ├── events/           # ProductCreated, StockUpdated
│       │   ├── services/         # Domain Services
│       │   └── ports/            # Repository + Search Port
│       ├── application/
│       └── infrastructure/
│           └── search/           # Elasticsearch Adapter
└── src/test/java/

order-service/                    # 訂單管理服務
├── src/main/java/
│   └── com/ecommerce/order/
│       ├── domain/
│       │   ├── entities/         # Order, OrderItem
│       │   ├── value_objects/    # OrderId, OrderStatus, Money
│       │   ├── aggregates/       # OrderAggregate
│       │   ├── events/           # OrderCreated, OrderPaid, OrderCancelled
│       │   ├── services/         # Domain Services
│       │   └── ports/
│       ├── application/
│       └── infrastructure/
│           └── messaging/        # 事件發布 Adapter（Kafka/RabbitMQ）
└── src/test/java/

payment-service/                  # 付款管理服務
├── src/main/java/
│   └── com/ecommerce/payment/
│       ├── domain/
│       │   ├── entities/         # Payment, Refund
│       │   ├── value_objects/    # PaymentMethod, TransactionId
│       │   ├── aggregates/       # PaymentAggregate
│       │   ├── events/           # PaymentCompleted, RefundInitiated
│       │   ├── services/         # Domain Services
│       │   └── ports/            # PaymentGateway Port
│       ├── application/
│       └── infrastructure/
│           └── gateway/          # 第三方付款閘道 Adapter
└── src/test/java/

logistics-service/                # 物流管理服務
├── src/main/java/
│   └── com/ecommerce/logistics/
│       ├── domain/
│       │   ├── entities/         # Shipment, DeliveryStatus
│       │   ├── value_objects/    # TrackingNumber, Carrier
│       │   ├── aggregates/       # ShipmentAggregate
│       │   ├── events/           # ShipmentCreated, StatusUpdated
│       │   ├── services/         # Domain Services
│       │   └── ports/            # LogisticsProvider Port
│       ├── application/
│       └── infrastructure/
│           └── provider/         # 物流商 API Adapter
└── src/test/java/

sales-service/                    # 銷售管理服務
├── src/main/java/
│   └── com/ecommerce/sales/
│       ├── domain/
│       │   ├── entities/         # Promotion, Coupon
│       │   ├── value_objects/    # DiscountRule, CouponCode
│       │   ├── aggregates/       # PromotionAggregate
│       │   ├── events/           # CouponUsed, PromotionStarted
│       │   ├── services/         # Domain Services
│       │   └── ports/
│       ├── application/
│       └── infrastructure/
└── src/test/java/

admin-portal/                     # 後台管理系統（獨立專案）
├── backend/
│   └── src/main/java/
│       └── com/ecommerce/admin/
│           ├── domain/
│           ├── application/
│           └── infrastructure/
└── frontend/                     # 後台前端（可選：React/Vue）
    ├── src/
    └── tests/

shared-kernel/                    # 共享核心（跨服務共用的 Domain 物件）
├── src/main/java/
│   └── com/ecommerce/shared/
│       ├── domain/
│       │   ├── events/           # 共用領域事件基礎類別
│       │   └── value_objects/    # 共用 Value Objects（Money, Address）
│       └── infrastructure/
│           └── events/           # 事件序列化工具
└── src/test/java/

infrastructure/                   # 基礎設施配置（Docker、K8s）
├── docker/
│   ├── docker-compose.yml        # 本地開發環境
│   └── docker-compose.test.yml   # 測試環境
└── k8s/
    └── manifests/                # Kubernetes 部署配置
```

**Structure Decision**: 採用微服務多專案結構，每個服務獨立遵循六角形架構（domain/application/infrastructure 三層），透過 shared-kernel 共享基礎 Domain 物件，security-infrastructure 模組提供統一的安全基礎設施。

## Complexity Tracking

> **所有設計決策皆符合 Constitution，無需記錄 Violation**

| 設計決策 | 理由 | 符合原則 |
|----------|------|----------|
| 微服務架構（7+ 服務） | 業務需求定義 6 個獨立 Bounded Context | DDD |
| 獨立 security-infrastructure 模組 | 安全邏輯與業務邏輯解耦，可跨服務重用 | SOLID（SRP, DIP） |
| 事件驅動跨服務通訊 | 服務間鬆耦合，符合 FR-028 需求 | DDD Domain Events |
| Elasticsearch 用於商品搜尋 | 滿足 < 1s 搜尋效能需求 | 效能目標 |
