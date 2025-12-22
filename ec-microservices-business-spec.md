# 電商微服務系統 業務規格文件

> **文件版本**: 1.0  
> **建立日期**: 2025-12-21  
> **來源 Repository**: https://github.com/ChunPingWang/ec-microservices

---

## 1. 專案概述

### 1.1 專案背景

本專案為一套基於微服務架構的電子商務平台系統，採用現代化的軟體設計原則和技術棧，旨在提供高可擴展性、高可維護性的電商解決方案。

### 1.2 專案目標

- 建構模組化、鬆耦合的微服務系統
- 實現領域驅動設計（DDD）與六角形架構（Hexagonal Architecture）
- 嚴格遵循 SOLID 設計原則
- 支援多環境部署（開發/SIT/UAT/生產）
- 採用測試先行開發方法論（TDD/BDD）

---

## 2. 系統架構

### 2.1 整體架構圖

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│                   (Web / Mobile / Third-party)                   │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                     API Gateway (Port: 8080)                     │
│              統一入口、路由、認證、限流、負載均衡                    │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────┬───────────┼───────────┬───────────┐
        ▼           ▼           ▼           ▼           ▼
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│  Customer   │ │   Product   │ │    Order    │ │   Payment   │
│   Service   │ │   Service   │ │   Service   │ │   Service   │
│  (8081)     │ │  (8082)     │ │  (8083)     │ │  (8084)     │
└─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘
        │                               │               │
        └───────────────────────────────┼───────────────┘
                                        │
        ┌───────────────────────────────┴───────────────┐
        ▼                                               ▼
┌─────────────┐                                 ┌─────────────┐
│  Logistics  │                                 │    Sales    │
│   Service   │                                 │   Service   │
│  (8085)     │                                 │  (8086)     │
└─────────────┘                                 └─────────────┘
```

### 2.2 微服務清單

| 服務名稱 | Port | 職責描述 |
|---------|------|---------|
| API Gateway | 8080 | 統一入口、路由轉發、認證授權、限流熔斷 |
| Customer Service | 8081 | 客戶管理、會員資訊、認證授權 |
| Product Service | 8082 | 商品管理、庫存管理、商品分類 |
| Order Service | 8083 | 訂單管理、訂單狀態、訂單查詢 |
| Payment Service | 8084 | 付款處理、支付整合、退款管理 |
| Logistics Service | 8085 | 物流追蹤、配送管理、倉儲管理 |
| Sales Service | 8086 | 銷售統計、促銷活動、行銷管理 |

---

## 3. 業務領域分析

### 3.1 Customer Service（客戶服務）

#### 3.1.1 業務範圍

客戶服務負責管理所有與客戶相關的業務邏輯，包括客戶註冊、資訊管理、認證授權等功能。

#### 3.1.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 客戶註冊 | 新客戶建立帳號 | Email 唯一性驗證、密碼強度檢查 |
| 客戶登入 | 身份認證 | JWT Token 產生、Session 管理 |
| 個人資料管理 | 維護客戶基本資訊 | 必填欄位驗證、格式檢查 |
| 收貨地址管理 | 管理多組收貨地址 | 預設地址設定、地址數量上限 |
| 會員等級管理 | 依消費累積升降等級 | 等級計算規則、權益對應 |

#### 3.1.3 領域模型

```
Customer (聚合根)
├── customerId: UUID
├── email: String (唯一)
├── password: String (加密)
├── name: String
├── phone: String
├── memberLevel: MemberLevel
├── status: CustomerStatus
├── addresses: List<Address>
└── createdAt / updatedAt: DateTime
```

### 3.2 Product Service（商品服務）

#### 3.2.1 業務範圍

商品服務管理電商平台的所有商品資訊，包括商品基本資料、分類、庫存等。

#### 3.2.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 商品管理 | 商品 CRUD 操作 | SKU 唯一性、必填欄位驗證 |
| 分類管理 | 商品分類階層 | 最多三層分類、分類路徑維護 |
| 庫存管理 | 庫存數量追蹤 | 庫存預警、庫存鎖定機制 |
| 價格管理 | 商品定價與促銷價 | 價格歷史紀錄、多幣別支援 |
| 商品搜尋 | 關鍵字與條件搜尋 | 全文檢索、篩選條件組合 |

#### 3.2.3 領域模型

```
Product (聚合根)
├── productId: UUID
├── sku: String (唯一)
├── name: String
├── description: Text
├── category: Category
├── price: Money
├── inventory: Inventory
├── status: ProductStatus
├── images: List<ProductImage>
└── attributes: List<ProductAttribute>
```

### 3.3 Order Service（訂單服務）

#### 3.3.1 業務範圍

訂單服務處理整個訂單生命週期，從訂單建立到完成的所有狀態轉換。

#### 3.3.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 訂單建立 | 建立新訂單 | 庫存檢查、價格計算、優惠券驗證 |
| 訂單查詢 | 查詢訂單資訊 | 依客戶/日期/狀態查詢 |
| 訂單狀態管理 | 訂單狀態流轉 | 狀態機控制、狀態變更通知 |
| 訂單取消 | 取消訂單處理 | 取消時間限制、庫存回補 |
| 訂單統計 | 訂單數據統計 | 銷售報表、趨勢分析 |

#### 3.3.3 訂單狀態流程

```
[建立訂單] → [待付款] → [已付款] → [備貨中] → [已出貨] → [已送達] → [已完成]
                │           │           │           │
                ▼           ▼           ▼           ▼
            [已取消]    [付款失敗]   [缺貨取消]   [退貨退款]
```

#### 3.3.4 領域模型

```
Order (聚合根)
├── orderId: UUID
├── orderNumber: String (唯一)
├── customerId: UUID
├── items: List<OrderItem>
├── totalAmount: Money
├── shippingAddress: Address
├── status: OrderStatus
├── paymentInfo: PaymentInfo
├── createdAt / updatedAt: DateTime
└── version: Integer (樂觀鎖)
```

### 3.4 Payment Service（付款服務）

#### 3.4.1 業務範圍

付款服務整合各種支付方式，處理付款、退款等金流相關業務。

#### 3.4.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 付款處理 | 處理訂單付款 | 支付方式驗證、金額核對 |
| 退款處理 | 處理退款請求 | 退款原因記錄、部分退款支援 |
| 支付整合 | 第三方支付整合 | 信用卡/ATM/超商/電子支付 |
| 交易紀錄 | 金流交易歷史 | 交易追蹤、對帳功能 |

#### 3.4.3 領域模型

```
Payment (聚合根)
├── paymentId: UUID
├── orderId: UUID
├── amount: Money
├── paymentMethod: PaymentMethod
├── status: PaymentStatus
├── transactionId: String
├── paidAt: DateTime
└── refunds: List<Refund>
```

### 3.5 Logistics Service（物流服務）

#### 3.5.1 業務範圍

物流服務管理商品配送、物流追蹤、倉儲出入庫等業務。

#### 3.5.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 配送管理 | 配送單建立與管理 | 配送商分配、運費計算 |
| 物流追蹤 | 即時追蹤配送狀態 | 狀態更新通知、預計送達 |
| 倉儲管理 | 倉庫庫存管理 | 入庫/出庫/調撥作業 |
| 物流商整合 | 第三方物流整合 | 黑貓/新竹/超商取貨 |

#### 3.5.3 領域模型

```
Shipment (聚合根)
├── shipmentId: UUID
├── orderId: UUID
├── carrier: Carrier
├── trackingNumber: String
├── status: ShipmentStatus
├── estimatedDelivery: Date
├── events: List<ShipmentEvent>
└── deliveredAt: DateTime
```

### 3.6 Sales Service（銷售服務）

#### 3.6.1 業務範圍

銷售服務負責銷售數據分析、促銷活動管理、行銷推廣等業務。

#### 3.6.2 核心功能

| 功能模組 | 功能說明 | 業務規則 |
|---------|---------|---------|
| 促銷管理 | 建立管理促銷活動 | 促銷規則、活動時間控制 |
| 優惠券管理 | 優惠券發放與使用 | 使用條件、數量限制 |
| 銷售統計 | 銷售數據分析 | 即時統計、歷史報表 |
| 行銷活動 | 行銷推廣管理 | 活動追蹤、成效分析 |

#### 3.6.3 領域模型

```
Promotion (聚合根)
├── promotionId: UUID
├── name: String
├── type: PromotionType
├── rules: List<PromotionRule>
├── startDate / endDate: DateTime
├── status: PromotionStatus
└── usageLimit: Integer
```

---

## 4. 技術規格

### 4.1 技術棧

| 類別 | 技術選型 | 版本 |
|-----|---------|------|
| 程式語言 | Java | 17 |
| 應用框架 | Spring Boot | 3.2.0 |
| 建構工具 | Gradle | 8.x |
| 開發資料庫 | H2 | 記憶體模式 |
| 生產資料庫 | PostgreSQL | 15 |
| 快取 | Redis | - |
| 訊息佇列 | RabbitMQ | - |

### 4.2 測試框架

| 測試類型 | 框架/工具 | 用途 |
|---------|----------|------|
| 單元測試 | JUnit 5, Mockito | 領域邏輯測試 |
| BDD 測試 | Cucumber, Gherkin | 業務需求驗證 |
| 整合測試 | TestContainers | PostgreSQL 容器測試 |
| API 測試 | REST Assured | RESTful API 測試 |

### 4.3 架構設計原則

#### 4.3.1 SOLID 原則

| 原則 | 英文全稱 | 設計指導 |
|-----|---------|---------|
| SRP | Single Responsibility | 每個類別只有一個變更的理由 |
| OCP | Open/Closed | 對擴展開放，對修改封閉 |
| LSP | Liskov Substitution | 子類別可以替換父類別 |
| ISP | Interface Segregation | 客戶端不應依賴不需要的介面 |
| DIP | Dependency Inversion | 依賴抽象而非具體實作 |

#### 4.3.2 六角形架構

```
                    ┌─────────────────────────────────────┐
                    │         Infrastructure Layer         │
                    │   (技術實作、外部整合、資料庫存取)      │
                    │                                      │
                    │  ┌───────────────────────────────┐  │
                    │  │      Application Layer         │  │
                    │  │    (使用案例、應用服務)          │  │
                    │  │                                │  │
                    │  │  ┌───────────────────────┐    │  │
                    │  │  │    Domain Layer        │    │  │
                    │  │  │  (領域邏輯、業務規則)    │    │  │
                    │  │  │                        │    │  │
                    │  │  └───────────────────────┘    │  │
                    │  │                                │  │
                    │  └───────────────────────────────┘  │
                    │                                      │
                    └─────────────────────────────────────┘
```

---

## 5. 環境配置

### 5.1 環境清單

| 環境 | 用途 | 資料庫 | Profile |
|-----|------|-------|---------|
| 本機開發 | 開發測試 | H2 (記憶體) | default |
| SIT | 系統整合測試 | PostgreSQL | sit |
| UAT | 使用者驗收測試 | PostgreSQL | uat |
| Production | 正式環境 | PostgreSQL | prod |

### 5.2 H2 控制台連線

```
URL: http://localhost:8081/h2-console
JDBC URL: jdbc:h2:mem:customer_db
User Name: sa
Password: (空白)
```

### 5.3 啟動指令

```bash
# 本機開發環境 (預設)
./gradlew :customer-service:bootRun

# SIT 環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=sit'

# UAT 環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=uat'

# 生產環境
./gradlew :customer-service:bootRun --args='--spring.profiles.active=prod'
```

---

## 6. API 規格

### 6.1 API 文件端點

| 服務 | Swagger UI URL |
|-----|----------------|
| API Gateway | http://localhost:8080/swagger-ui.html |
| Customer Service | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082/swagger-ui.html |
| Order Service | http://localhost:8083/swagger-ui.html |
| Payment Service | http://localhost:8084/swagger-ui.html |
| Logistics Service | http://localhost:8085/swagger-ui.html |
| Sales Service | http://localhost:8086/swagger-ui.html |

### 6.2 監控端點

| 端點 | 用途 |
|-----|------|
| `/actuator/health` | 健康檢查 |
| `/actuator/metrics` | 效能指標 |
| `/actuator/info` | 應用資訊 |

---

## 7. 跨服務通訊

### 7.1 同步通訊

- 使用 REST API 進行服務間同步呼叫
- 透過 API Gateway 進行統一路由

### 7.2 非同步通訊

- 使用 RabbitMQ 作為訊息中介
- 事件驅動架構處理跨服務事件

### 7.3 事件清單

| 事件名稱 | 發布服務 | 訂閱服務 | 說明 |
|---------|---------|---------|------|
| OrderCreated | Order | Payment, Inventory | 訂單建立事件 |
| PaymentCompleted | Payment | Order, Logistics | 付款完成事件 |
| ShipmentUpdated | Logistics | Order, Notification | 物流狀態更新 |
| InventoryLow | Product | Sales, Notification | 庫存不足警示 |

---

## 8. 安全規格

### 8.1 認證授權

- JWT (JSON Web Token) 認證機制
- OAuth 2.0 授權框架
- API Gateway 統一認證

### 8.2 資料安全

- 密碼採用 BCrypt 加密儲存
- 敏感資料傳輸使用 TLS/SSL
- 資料庫連線加密

---

## 9. 測試策略

### 9.1 測試金字塔

```
        /\
       /  \        E2E Tests (少量)
      /────\
     /      \      Integration Tests (適量)
    /────────\
   /          \    Unit Tests (大量)
  /────────────\
```

### 9.2 測試執行

```bash
# BDD 功能測試
./gradlew :customer-service:test --tests "*CucumberTestRunner"

# 單元測試
./gradlew :customer-service:test --tests "*UnitTest"

# 整合測試
./gradlew :customer-service:test --tests "*IntegrationTest"

# 執行所有測試
./gradlew test
```

### 9.3 BDD 測試報告

| 格式 | 路徑 |
|-----|------|
| HTML | `target/cucumber-reports/index.html` |
| JSON | `target/cucumber-reports/Cucumber.json` |
| JUnit XML | `target/cucumber-reports/Cucumber.xml` |

---

## 10. 附錄

### 10.1 名詞解釋

| 名詞 | 說明 |
|-----|------|
| DDD | Domain-Driven Design，領域驅動設計 |
| Hexagonal Architecture | 六角形架構，又稱 Ports and Adapters |
| Aggregate Root | 聚合根，DDD 中的核心概念 |
| Bounded Context | 限界上下文，定義領域邊界 |

### 10.2 參考資源

- 專案 Repository: https://github.com/ChunPingWang/ec-microservices
- Spring Boot 官方文件: https://spring.io/projects/spring-boot
- Domain-Driven Design 參考: https://domainlanguage.com/ddd/

---

## 11. 專案決策與澄清

### 11.1 架構決策記錄 (2025-12-23)

以下為專案規劃階段確認的關鍵決策：

| # | 問題 | 決策 | 影響範圍 |
|---|------|------|----------|
| 1 | 系統可用性目標為何？ | **99.99% 可用性**（年停機 <53 分鐘），自動故障轉移 | 需要多區部署、健康檢查、自動容錯機制 |
| 2 | 敏感資料保護策略為何？ | **完整 PCI DSS 合規**，自行加密儲存卡號，定期安全稽核 | 需要 AES-256 加密、審計日誌、季度安全稽核 |
| 3 | 後台管理系統是否在範圍內？ | **包含完整後台**（商品管理、訂單管理、報表、促銷設定） | 需要管理員角色權限、後台 API 與介面 |

### 11.2 可用性要求詳細規格

基於 99.99% 可用性目標，系統需滿足：

- **年度停機時間**：< 53 分鐘
- **故障恢復時間 (RTO)**：< 1 分鐘（自動故障轉移）
- **資料恢復點 (RPO)**：< 1 分鐘
- **健康檢查間隔**：每 10 秒
- **自動擴展**：依據 CPU/Memory 使用率觸發

### 11.3 PCI DSS 合規要求

| 需求編號 | 要求說明 | 實作方式 |
|---------|---------|---------|
| FR-030 | 符合 PCI DSS 資料安全標準 | 完整合規認證 |
| FR-031 | 加密儲存信用卡資訊 | AES-256 加密 |
| FR-032 | 傳輸層加密 | TLS 1.2+ |
| FR-033 | 敏感資料存取審計 | 審計日誌記錄 |
| FR-034 | 定期安全稽核 | 至少每季一次 |
| FR-035 | 密碼雜湊儲存 | BCrypt 或 Argon2 |

### 11.4 後台管理系統功能範圍

| 功能模組 | 說明 | 權限角色 |
|---------|------|---------|
| 商品管理 | 新增、編輯、上下架商品 | 商品管理員、超級管理員 |
| 訂單管理 | 查看、處理、更新訂單狀態 | 訂單管理員、超級管理員 |
| 促銷管理 | 建立、編輯、啟停促銷活動與優惠券 | 行銷管理員、超級管理員 |
| 銷售報表 | 查看銷售數據（日/週/月/自訂區間） | 報表檢視者、超級管理員 |
| 客戶管理 | 查詢、停用、重設密碼 | 客服管理員、超級管理員 |
| 庫存管理 | 調整數量、設定預警值 | 倉管管理員、超級管理員 |

---

*本文件依據 ec-microservices 專案結構與 README 自動產生，如需更詳細的 API 規格或領域模型，請參考各服務的原始碼。*
