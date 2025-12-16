<!--
Sync Impact Report
==================
Version change: 0.0.0 → 1.0.0
Bump rationale: MAJOR - Initial constitution creation from user-provided principles

Modified principles: N/A (initial creation)

Added sections:
- Core Principles (6 principles total)
  - I. 程式碼品質 (Code Quality)
  - II. 測試優先 (Test-First - TDD/BDD)
  - III. 契約驅動開發 (Contract-Driven Development)
  - IV. 領域驅動設計 (Domain-Driven Design)
  - V. SOLID 原則
  - VI. 六角形架構 (Hexagonal Architecture)
- 架構約束 (Architectural Constraints)
- 開發工作流程 (Development Workflow)
- Governance

Removed sections: N/A (initial creation)

Templates requiring updates:
- .specify/templates/plan-template.md ✅ (Constitution Check section aligned)
- .specify/templates/spec-template.md ✅ (No changes required)
- .specify/templates/tasks-template.md ✅ (No changes required)
- .specify/templates/checklist-template.md ✅ (No changes required)

Follow-up TODOs: None
-->

# Design by Contract Spring Cloud Contract Constitution

## Core Principles

### I. 程式碼品質 (Code Quality)

所有程式碼 MUST 遵循以下品質標準：

- **可讀性**: 程式碼 MUST 自我文件化，命名清晰明確
- **可維護性**: 程式碼 MUST 易於理解、修改和擴展
- **一致性**: 專案內 MUST 保持統一的編碼風格和命名慣例
- **簡潔性**: 避免過度工程化，實作 MUST 符合當前需求的最小複雜度

**Rationale**: 高品質程式碼降低維護成本，提高團隊協作效率，減少技術債務累積。

### II. 測試優先 (Test-First - TDD/BDD)

測試驅動開發為本專案的核心開發方法：

- **TDD 流程**: MUST 遵循 Red-Green-Refactor 循環
  1. 先撰寫失敗的測試 (Red)
  2. 撰寫最小程式碼使測試通過 (Green)
  3. 重構改善程式碼品質 (Refactor)
- **BDD 規格**: 使用者故事 MUST 採用 Given-When-Then 格式定義驗收條件
- **契約測試**: 服務間通訊 MUST 透過 Spring Cloud Contract 定義和驗證契約
- **測試覆蓋**: 核心業務邏輯 MUST 有對應的單元測試和整合測試

**Rationale**: 測試優先確保程式碼符合需求規格，及早發現問題，提供重構的安全網。

### III. 契約驅動開發 (Contract-Driven Development)

本專案採用 Design by Contract 原則和 Spring Cloud Contract 實現服務間契約：

- **前置條件 (Precondition)**: 每個 API MUST 明確定義輸入條件和驗證規則
- **後置條件 (Postcondition)**: 每個 API MUST 明確定義預期輸出和狀態變化
- **不變量 (Invariant)**: 系統 MUST 定義並維護核心業務不變量
- **契約格式**: 契約定義 MUST 使用 Groovy DSL 撰寫，包含完整的 request/response 規格
- **契約同步**: Provider 變更契約時 MUST 確保所有 Consumer 仍能正常運作

**Rationale**: 契約驅動開發確保服務間的一致性，實現真正的解耦和獨立部署能力。

### IV. 領域驅動設計 (Domain-Driven Design)

業務邏輯 MUST 採用 DDD 戰術模式組織：

- **領域模型**: 核心業務概念 MUST 以 Entity、Value Object、Aggregate 表達
- **領域服務**: 跨 Aggregate 的業務邏輯 MUST 封裝在 Domain Service
- **Repository**: 資料存取 MUST 透過 Repository 介面抽象，隱藏持久化細節
- **Ubiquitous Language**: 程式碼命名 MUST 使用領域通用語言，與業務專家保持一致

**Rationale**: DDD 確保軟體模型反映真實業務需求，提高領域專家和開發人員的溝通效率。

### V. SOLID 原則

所有程式碼設計 MUST 遵守 SOLID 原則：

- **S - 單一職責原則**: 每個類別 MUST 只有一個改變的理由
- **O - 開放封閉原則**: 對擴展開放，對修改封閉
- **L - 里氏替換原則**: 子類別 MUST 能完全替換父類別使用
- **I - 介面隔離原則**: 客戶端不應被迫依賴不使用的介面
- **D - 依賴反轉原則**: 高層模組 MUST NOT 依賴低層模組，兩者都應依賴抽象

**Rationale**: SOLID 原則產生靈活、可維護、可測試的程式碼結構。

### VI. 六角形架構 (Hexagonal Architecture)

系統架構 MUST 採用六角形架構（Ports and Adapters）：

- **領域核心**: 業務邏輯 MUST 位於內層，不依賴任何框架或基礎設施
- **Ports（埠）**: 內層透過 Port 介面定義與外界的互動契約
- **Adapters（轉接器）**: 框架、資料庫、外部服務等 MUST 實作為外層 Adapter
- **依賴方向**: 依賴 MUST 從外層指向內層，MUST NOT 反向
- **框架隔離**: Spring、JPA 等框架相關程式碼 MUST 限制在 infrastructure 層

```
┌─────────────────────────────────────────┐
│           Infrastructure Layer           │
│  (Controllers, Repositories, Clients)    │
├─────────────────────────────────────────┤
│           Application Layer              │
│     (Use Cases, Application Services)    │
├─────────────────────────────────────────┤
│             Domain Layer                 │
│   (Entities, Value Objects, Services)    │
└─────────────────────────────────────────┘
```

**Rationale**: 六角形架構使核心業務邏輯獨立於技術實作，便於測試和技術遷移。

## 架構約束

### 技術棧規範

- **語言**: Java 17+
- **框架**: Spring Boot 3.x, Spring Cloud Contract 4.x
- **測試**: JUnit 5, AssertJ, Mockito, Rest Assured
- **建置**: Maven
- **CI/CD**: Gitea Actions

### 分層規則

| 層級 | 允許依賴 | 禁止依賴 |
|------|---------|---------|
| Domain | 無外部依賴 | Spring, JPA, 任何框架 |
| Application | Domain | 具體實作 |
| Infrastructure | Domain, Application, 框架 | - |

### 契約規範

- 契約檔案 MUST 放置於 `src/test/resources/contracts/` 目錄
- 契約命名 MUST 採用 `動詞+名詞.groovy` 格式（如 `getAccount.groovy`）
- 每個契約 MUST 包含 `name`、`description`、`request`、`response` 區塊
- `description` MUST 說明前置條件、後置條件、不變量

## 開發工作流程

### Git 版本控制

- 每個階段 MUST 進行版本控制
- Commit 訊息 MUST 使用繁體中文 (zh-TW)
- Commit 訊息 SHOULD 包含相關的 prompt 或需求描述，以便後續參考
- Commit 格式: `<類型>: <描述>`
  - 類型: `feat` (功能)、`fix` (修復)、`refactor` (重構)、`test` (測試)、`docs` (文件)

### 開發循環

1. **需求理解**: 確認使用者故事和驗收條件
2. **契約定義**: 定義 API 契約（Provider-Consumer 協商）
3. **測試撰寫**: 根據契約撰寫失敗測試
4. **實作**: 撰寫最小程式碼通過測試
5. **重構**: 改善程式碼品質，確保測試仍通過
6. **提交**: 進行 git commit，包含變更說明

## Governance

### 修訂程序

1. 任何原則變更 MUST 先提出書面修訂提案
2. 修訂 MUST 經過團隊審查和核准
3. 重大變更（原則新增/移除）MUST 更新所有相關文件和範本
4. 版本變更遵循語意化版本規範 (SemVer)

### 合規審查

- 所有 PR/Code Review MUST 驗證是否符合本憲法原則
- 違反原則的程式碼 MUST NOT 合併
- 複雜度增加 MUST 有明確的業務理由和文件說明

### 版本規範

- **MAJOR**: 不相容的治理/原則移除或重新定義
- **MINOR**: 新增原則/章節或實質性擴展指引
- **PATCH**: 澄清、措辭、錯字修正、非語意性調整

**Version**: 1.0.0 | **Ratified**: 2025-12-16 | **Last Amended**: 2025-12-16
