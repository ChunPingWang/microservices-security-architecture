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
