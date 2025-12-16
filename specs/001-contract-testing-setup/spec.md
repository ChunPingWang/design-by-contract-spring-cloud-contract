# Feature Specification: Spring Cloud Contract 契約測試系統

**Feature Branch**: `001-contract-testing-setup`
**Created**: 2025-12-16
**Status**: Draft
**Input**: 實作 Spring Cloud Contract 契約測試系統，包含帳戶服務 (Provider) 和支付服務 (Consumer) 的契約定義、驗證測試、Stub 發布和 CI/CD 自動化整合

## Clarifications

### Session 2025-12-16

- Q: 帳戶狀態轉換規則為何？ → A: 完整狀態機 (PENDING → ACTIVE ↔ FROZEN → CLOSED)，需驗證所有轉換
- Q: 契約測試失敗通知方式為何？ → A: 版控平台通知 (PR 評論/狀態檢查)
- Q: 契約測試的可觀測性需求為何？ → A: 完整可觀測性 (日誌 + 指標 + 追蹤)
- Q: 外部儲存庫連線失敗的處理策略為何？ → A: 重試機制 (最多 3 次)，失敗後使用快取
- Q: 契約中敏感資料的處理方式為何？ → A: 使用遮罩/虛擬資料 (如 ACC-XXX, ***)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Provider 定義契約 (Priority: P1)

作為帳戶服務 (account-service) 的開發人員，我需要定義 API 契約，以確保服務與消費者之間有明確的介面約定，並自動產生測試和 Stub。

**Why this priority**: 契約定義是整個契約測試系統的基礎。沒有契約定義，Consumer 無法進行契約測試，CI/CD 流程也無法運作。

**Independent Test**: 可以透過執行契約產生測試指令驗證契約是否正確產生測試類別和 Stub。

**Acceptance Scenarios**:

1. **Given** 帳戶服務專案已設置契約測試依賴，**When** 開發人員在契約目錄新增契約檔案，**Then** 系統能夠解析契約並產生對應的測試類別

2. **Given** 契約定義包含 Precondition、Postcondition 和 Invariant 說明，**When** 執行契約測試，**Then** 產生的測試會驗證 API 行為符合契約規範

3. **Given** 契約測試通過，**When** 執行建置，**Then** 系統會產生 Stub 檔案供 Consumer 使用

---

### User Story 2 - Consumer 契約測試 (Priority: P2)

作為支付服務 (payment-service) 的開發人員，我需要使用帳戶服務提供的 Stub 進行契約測試，以確保我的服務能正確呼叫帳戶服務的 API。

**Why this priority**: Consumer 契約測試是驗證服務間整合的關鍵環節，確保 Consumer 的實作與 Provider 的契約一致。

**Independent Test**: 可以透過執行 Consumer 端的契約測試，使用 Stub Runner 自動啟動 Stub 服務並驗證呼叫行為。

**Acceptance Scenarios**:

1. **Given** 帳戶服務的 Stub 已發布，**When** 支付服務執行契約測試並設定 Stub Runner，**Then** Stub Runner 自動下載並啟動 Stub 服務

2. **Given** Stub 服務運行中，**When** 支付服務的 HTTP Client 呼叫帳戶查詢功能，**Then** 收到符合契約定義的回應

3. **Given** 契約定義了餘額不足的錯誤場景，**When** 支付服務呼叫扣款功能且金額超過餘額，**Then** 收到錯誤回應及錯誤訊息

---

### User Story 3 - CI/CD 自動化驗證 (Priority: P3)

作為 DevOps 工程師，我需要在 CI/CD 流程中自動執行契約測試，以確保每次程式碼變更都符合契約規範，並自動發布 Stub。

**Why this priority**: CI/CD 自動化確保契約測試的持續執行，防止破壞性變更進入生產環境。

**Independent Test**: 可以透過觸發 CI Pipeline 並檢查契約測試結果和 Stub 發布狀態來驗證。

**Acceptance Scenarios**:

1. **Given** Provider 推送程式碼到主分支，**When** CI Pipeline 執行，**Then** 系統自動執行契約測試、產生 Stub 並發布到儲存庫

2. **Given** Consumer 推送程式碼，**When** CI Pipeline 執行契約測試，**Then** 系統自動下載最新的 Provider Stub 並執行測試

3. **Given** 契約測試失敗，**When** CI Pipeline 完成，**Then** 開發人員透過版控平台 (PR 評論/狀態檢查) 收到失敗通知並阻止合併

---

### User Story 4 - 契約版本管理與相容性 (Priority: P4)

作為開發團隊負責人，我需要管理契約版本，確保 Provider 的變更不會破壞現有 Consumer 的整合。

**Why this priority**: 契約版本管理確保向後相容性，避免生產環境的服務中斷。

**Independent Test**: 可以透過定期執行契約驗證工作流程，驗證所有 Consumer 仍能正常運作。

**Acceptance Scenarios**:

1. **Given** Provider 修改契約新增欄位，**When** 所有 Consumer 執行契約測試，**Then** 測試仍通過（向後相容）

2. **Given** Provider 移除契約中的必要欄位，**When** Consumer 執行契約測試，**Then** 測試失敗並提示不相容變更

3. **Given** 排程的契約驗證工作流程執行，**When** 發現不相容變更，**Then** 系統通知相關團隊

---

### Edge Cases

- 當 Provider Stub 尚未發布，Consumer 執行契約測試時，系統提示找不到 Stub 並指引解決方式
- 當契約定義語法錯誤，系統在建置階段提供明確的錯誤訊息和修正建議
- 當儲存庫無法連線，CI Pipeline 執行重試機制 (最多 3 次)，若仍失敗則使用本地快取的 Stub 並發出警告
- 當多個 Provider 版本的 Stub 同時存在，Consumer 可指定使用特定版本

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Provider 服務 MUST 支援使用契約定義語言定義 API 契約
- **FR-002**: 契約定義 MUST 包含請求方法、URL、Header、Body 和回應格式
- **FR-003**: 系統 MUST 從契約自動產生 Provider 端的驗證測試
- **FR-004**: 系統 MUST 從契約自動產生 Stub 供 Consumer 使用
- **FR-005**: Consumer 服務 MUST 能透過 Stub Runner 自動下載和啟動 Stub
- **FR-006**: CI Pipeline MUST 在每次建置時執行契約測試
- **FR-007**: 契約測試失敗時 MUST 阻止程式碼合併到主分支
- **FR-008**: Provider MUST 能將 Stub 發布到儲存庫
- **FR-009**: Consumer MUST 能從儲存庫下載 Stub
- **FR-010**: 契約 MUST 支援定義成功和錯誤場景
- **FR-011**: 契約 MUST 支援使用正則表達式和 Matcher 進行彈性驗證
- **FR-012**: 系統 MUST 支援契約測試的本地執行和 CI 環境執行
- **FR-013**: 系統 MUST 提供完整可觀測性支援，包含結構化日誌、執行指標和分散式追蹤
- **FR-014**: 契約定義中的敏感資料 MUST 使用遮罩/虛擬資料 (如 ACC-XXX, ***) 以確保安全性

### Key Entities

- **Contract（契約）**: 定義 Provider API 的行為規格，包含請求格式、回應格式、前置條件和後置條件
- **Stub**: 從契約產生的模擬服務，供 Consumer 在不依賴真實 Provider 的情況下進行測試
- **Provider（服務提供者）**: 提供功能服務的微服務，如帳戶服務
- **Consumer（服務消費者）**: 呼叫其他服務功能的微服務，如支付服務
- **Account（帳戶）**: 代表用戶的金融帳戶，包含帳戶識別碼、持有人、餘額、幣別、狀態。狀態遵循完整狀態機：PENDING → ACTIVE ↔ FROZEN → CLOSED，契約測試需驗證所有狀態轉換
- **DebitTransaction（扣款交易）**: 代表一筆扣款操作，包含交易識別碼、金額、狀態

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Provider 開發人員能在 30 分鐘內完成新契約的定義和驗證測試
- **SC-002**: Consumer 開發人員能在 15 分鐘內設置 Stub Runner 並執行契約測試
- **SC-003**: CI Pipeline 完整執行（建置、測試、Stub 發布）時間不超過 10 分鐘
- **SC-004**: 契約變更導致的問題在 CI 階段被檢測出來，不會進入生產環境
- **SC-005**: 90% 以上的服務間整合問題能透過契約測試提前發現
- **SC-006**: 新加入團隊的開發人員能在 1 小時內理解並開始使用契約測試系統
- **SC-007**: 契約測試系統支援同時運行多個 Consumer 的測試，不產生衝突

## Assumptions

- 儲存庫 (Artifact Repository) 已建置並可供使用
- CI/CD 系統已設置並有執行測試的權限
- 開發人員熟悉建置工具和基本的微服務開發
- 網路環境允許服務間通訊和儲存庫存取
