<!--
Sync Impact Report
==================
Version change: 1.0.0 (initial)
Added sections:
  - 8 Core Principles (TDD, BDD, DDD, SOLID, Hexagonal Architecture, Code Quality, Testing Standards, Commit Discipline)
  - Architecture Constraints section
  - Development Workflow section
  - Governance section
Removed sections: None (initial version)
Templates requiring updates:
  - .specify/templates/plan-template.md: ✅ updated (Constitution Check gates)
  - .specify/templates/spec-template.md: ✅ updated (BDD scenarios mandatory)
  - .specify/templates/tasks-template.md: ✅ updated (TDD workflow enforced)
Follow-up TODOs: None
==================
-->

# Security for Microservices Constitution

## Core Principles

### I. 測試驅動開發 (Test-Driven Development, TDD) - 非妥協原則

所有功能開發必須遵循 TDD 紅綠重構循環：

1. **紅燈**：先撰寫失敗的測試案例，定義預期行為
2. **綠燈**：撰寫最少量的程式碼使測試通過
3. **重構**：在測試保護下改善程式碼品質

**強制規範**：
- 測試必須在實作程式碼之前提交
- 測試案例必須先執行並確認失敗
- 禁止未經測試覆蓋的產線程式碼
- 測試覆蓋率目標：核心邏輯 ≥ 80%

**理由**：測試先行確保設計由需求驅動，而非由實作細節驅動，並提供持續重構的安全網。

### II. 行為驅動開發 (Behavior-Driven Development, BDD)

使用者故事必須以 Gherkin 語法定義驗收情境：

```gherkin
Given [前置條件]
When [使用者操作]
Then [預期結果]
```

**強制規範**：
- 每個 User Story 必須包含至少一個 BDD 情境
- 情境必須可獨立執行與驗證
- 使用領域語言描述，非技術術語
- 端對端測試必須對應 BDD 情境

**理由**：BDD 確保開發團隊與利害關係人對需求有共同理解，減少溝通落差。

### III. 領域驅動設計 (Domain-Driven Design, DDD)

程式碼結構必須反映業務領域模型：

**強制規範**：
- 核心業務邏輯必須封裝於 Domain 層
- 使用 Ubiquitous Language（通用語言）命名
- Aggregate、Entity、Value Object 必須明確定義
- Domain Events 用於跨 Aggregate 通訊
- Repository 介面定義於 Domain 層

**架構分層**：
```
domain/          # 純業務邏輯，無外部依賴
├── entities/
├── value_objects/
├── aggregates/
├── events/
└── repositories/ (interfaces only)
```

**理由**：DDD 確保程式碼結構與業務需求對齊，降低維護成本與認知負擔。

### IV. SOLID 原則

所有程式碼必須遵守 SOLID 設計原則：

- **S**ingle Responsibility：每個類別只有一個變更理由
- **O**pen/Closed：對擴展開放，對修改封閉
- **L**iskov Substitution：子類別必須可替換父類別
- **I**nterface Segregation：介面應精簡，避免胖介面
- **D**ependency Inversion：依賴抽象而非具體實作

**強制規範**：
- Code Review 必須檢查 SOLID 違規
- 違規必須於 PR 中說明理由並記錄技術債
- 優先使用組合（Composition）而非繼承

**理由**：SOLID 原則確保程式碼具備高內聚、低耦合特性，易於測試與維護。

### V. 六角形架構 (Hexagonal Architecture)

採用 Ports and Adapters 架構模式：

```
┌─────────────────────────────────────────┐
│            Infrastructure               │
│  (Frameworks, Databases, External APIs) │
│  ┌───────────────────────────────────┐  │
│  │           Application             │  │
│  │  ┌─────────────────────────────┐  │  │
│  │  │         Domain              │  │  │
│  │  │   (Pure Business Logic)     │  │  │
│  │  └─────────────────────────────┘  │  │
│  └───────────────────────────────────┘  │
└─────────────────────────────────────────┘
```

**強制規範**：
- **Domain 層（內圈）**：純業務邏輯，零框架依賴
- **Application 層（中圈）**：Use Cases、Application Services
- **Infrastructure 層（外圈）**：框架、資料庫、外部服務

**依賴規則**：
- 內層絕不依賴外層
- 外層透過 Port（介面）與內層互動
- Adapter 實作 Port 介面
- 依賴注入（DI）用於連接層級

**專案結構**：
```
src/
├── domain/           # 內圈：純業務邏輯
│   ├── entities/
│   ├── value_objects/
│   ├── services/     # Domain Services
│   └── ports/        # 定義介面 (inbound + outbound)
├── application/      # 中圈：Use Cases
│   ├── use_cases/
│   └── services/     # Application Services
└── infrastructure/   # 外圈：框架與實作
    ├── adapters/     # Port 實作
    ├── persistence/  # Repository 實作
    ├── web/          # Controllers, API
    └── config/       # 框架設定
```

**理由**：六角形架構使核心業務邏輯與技術細節解耦，便於測試與技術替換。

### VI. 程式碼品質標準

**強制規範**：
- 遵循語言慣例的命名規範
- 函式/方法行數上限：30 行
- 類別行數上限：300 行
- 認知複雜度上限：15
- 巢狀深度上限：4 層
- 參數數量上限：5 個

**靜態分析**：
- Linter 必須通過無警告
- 型別檢查必須通過（TypeScript strict、mypy 等）
- 程式碼格式化必須一致

**文件要求**：
- 公開 API 必須有文件
- 複雜演算法必須有註解說明
- README 必須包含快速開始指南

**理由**：一致的品質標準降低認知負擔，提升團隊協作效率。

### VII. 測試標準

**測試金字塔**：
```
        /\
       /E2E\        少量：使用者情境驗證
      /──────\
     /Integration\  適量：模組整合驗證
    /──────────────\
   /    Unit Tests   \  大量：單元邏輯驗證
  /────────────────────\
```

**強制規範**：
- Unit Tests：測試 Domain 層，禁用 Mock 框架依賴
- Integration Tests：測試 Adapter 與外部系統整合
- Contract Tests：驗證 API 契約
- E2E Tests：對應 BDD 情境

**測試品質**：
- 測試必須獨立、可重複執行
- 測試名稱必須描述行為，非實作
- 禁止測試間共享可變狀態
- 測試執行時間目標：單元測試 < 100ms/case

**理由**：分層測試策略平衡測試覆蓋與執行效率。

### VIII. 提交紀律 (Commit Discipline)

**強制規範**：
- 每完成一個 Task 必須立即提交
- Commit 訊息必須使用繁體中文
- Commit 訊息格式：`類型: 簡短描述`
- 類型包含：`功能`、`修復`、`重構`、`測試`、`文件`、`設定`

**範例**：
```
功能: 新增使用者認證 JWT 驗證機制
測試: 新增認證服務單元測試
修復: 修正 Token 過期驗證邏輯
重構: 抽取認證邏輯至獨立 Domain Service
```

**禁止事項**：
- 禁止一次提交多個無關變更
- 禁止提交未通過測試的程式碼
- 禁止提交含有 TODO/FIXME 的程式碼至主分支

**理由**：細粒度提交便於追溯變更、Code Review 與問題回溯。

## Architecture Constraints

### 框架隔離原則

所有外部框架必須限制於 Infrastructure 層：

**強制規範**：
- Spring Framework：僅限 `infrastructure/` 目錄
- Database ORM：Repository 實作於 `infrastructure/persistence/`
- Web Framework：Controllers 於 `infrastructure/web/`
- Message Queue：Adapter 於 `infrastructure/messaging/`

**依賴反轉實踐**：
```java
// ✓ 正確：Domain 定義介面
// domain/ports/UserRepository.java
public interface UserRepository {
    User findById(UserId id);
}

// infrastructure/persistence/JpaUserRepository.java
@Repository
public class JpaUserRepository implements UserRepository {
    // Spring JPA 實作
}

// ✗ 錯誤：Domain 依賴框架
// domain/entities/User.java
@Entity  // 禁止！Domain 不應有 JPA 註解
public class User { ... }
```

### 安全約束

作為安全微服務專案：
- 安全邏輯必須為一等公民（First-class citizen）
- 認證/授權必須透過 Domain Service 抽象
- 敏感資料處理必須有審計追蹤
- 密碼/金鑰禁止硬編碼

## Development Workflow

### 開發流程

1. **需求分析**：使用 BDD 情境定義驗收標準
2. **設計**：遵循 DDD 識別 Domain Model
3. **TDD 循環**：
   - 撰寫失敗測試
   - 實作最小程式碼
   - 重構
   - Git Commit（繁體中文訊息）
4. **Code Review**：檢查 SOLID、六角形架構遵循度
5. **整合**：確保所有測試通過

### Pull Request 檢查清單

- [ ] 所有測試通過
- [ ] 程式碼符合六角形架構分層
- [ ] Domain 層無框架依賴
- [ ] 遵循 SOLID 原則
- [ ] Commit 訊息使用繁體中文
- [ ] 覆蓋率未下降

## Governance

### 憲法效力

本憲法為專案最高開發準則，優先於所有其他慣例與指南。

### 修訂程序

1. 提出修訂 Pull Request
2. 團隊成員審查與討論
3. 至少一位核心成員批准
4. 更新版本號與修訂日期
5. 更新所有相關模板以保持一致

### 版本管理

- **MAJOR**：原則移除或重新定義
- **MINOR**：新增原則或重大擴充
- **PATCH**：釐清、措辭修正、非語意性調整

### 合規審查

- 每次 Code Review 必須驗證憲法遵循
- 違規必須記錄並說明理由
- 持續違規需提請架構決策會議

**Version**: 1.0.0 | **Ratified**: 2025-12-22 | **Last Amended**: 2025-12-22
