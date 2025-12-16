# Data Model: Spring Cloud Contract 契約測試系統

**Feature**: 001-contract-testing-setup
**Date**: 2025-12-16

## Entity Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           Account Service (Provider)                     │
│                                                                          │
│  ┌─────────────────────────┐         ┌─────────────────────────────┐    │
│  │        Account          │         │     DebitTransaction        │    │
│  ├─────────────────────────┤         ├─────────────────────────────┤    │
│  │ accountId: String (PK)  │         │ transactionId: String (PK)  │    │
│  │ accountHolder: String   │    1:N  │ accountId: String (FK)      │    │
│  │ balance: BigDecimal     │◄────────│ amount: BigDecimal          │    │
│  │ currency: String        │         │ currency: String            │    │
│  │ status: AccountStatus   │         │ status: TransactionStatus   │    │
│  │ createdAt: Instant      │         │ description: String         │    │
│  │ updatedAt: Instant      │         │ merchantId: String          │    │
│  └─────────────────────────┘         │ processedAt: Instant        │    │
│                                      └─────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                         Payment Service (Consumer)                       │
│                                                                          │
│  ┌─────────────────────────┐                                            │
│  │     PaymentRequest      │                                            │
│  ├─────────────────────────┤                                            │
│  │ paymentId: String       │                                            │
│  │ fromAccountId: String   │  ──► Account Service API                   │
│  │ amount: BigDecimal      │                                            │
│  │ currency: String        │                                            │
│  │ description: String     │                                            │
│  └─────────────────────────┘                                            │
└─────────────────────────────────────────────────────────────────────────┘
```

## Entities

### Account (帳戶)

**Description**: 代表用戶的金融帳戶，為 Account Service 的核心 Aggregate Root。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| accountId | String | PK, Format: `ACC-XXX` | 帳戶唯一識別碼 |
| accountHolder | String | Required, 2-50 chars | 帳戶持有人姓名 |
| balance | BigDecimal | Required, >= 0 | 帳戶餘額 |
| currency | String | Required, ISO 4217 | 幣別代碼 (如 TWD, USD) |
| status | AccountStatus | Required | 帳戶狀態 |
| createdAt | Instant | Required, Auto | 建立時間 (UTC) |
| updatedAt | Instant | Required, Auto | 更新時間 (UTC) |

**Validation Rules**:
- accountId 格式必須為 `ACC-` 後接數字
- balance 永遠不可為負數 (Invariant)
- currency 必須為有效的 ISO 4217 代碼

---

### AccountStatus (帳戶狀態)

**Description**: 帳戶狀態枚舉，定義帳戶生命週期。

| Value | Description | Allowed Transitions |
|-------|-------------|---------------------|
| PENDING | 待啟用 | → ACTIVE |
| ACTIVE | 啟用中 | → FROZEN, → CLOSED |
| FROZEN | 凍結中 | → ACTIVE, → CLOSED |
| CLOSED | 已關閉 | (終態，無法轉換) |

**State Machine**:
```
         ┌─────────┐
         │ PENDING │
         └────┬────┘
              │ activate()
              ▼
         ┌─────────┐
    ┌───►│  ACTIVE │◄───┐
    │    └────┬────┘    │
    │         │         │
    │ unfreeze()  freeze()
    │         │         │
    │    ┌────▼────┐    │
    └────│  FROZEN │────┘
         └────┬────┘
              │ close()
              ▼
         ┌─────────┐
         │ CLOSED  │
         └─────────┘
```

**Transition Rules**:
- PENDING → ACTIVE: 帳戶驗證完成後啟用
- ACTIVE ↔ FROZEN: 可疑活動時凍結，解除後恢復
- ACTIVE/FROZEN → CLOSED: 永久關閉，不可逆

---

### DebitTransaction (扣款交易)

**Description**: 代表一筆扣款操作，關聯到特定帳戶。

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| transactionId | String | PK, Format: `TXN-YYYYMMDD-XXX` | 交易唯一識別碼 |
| accountId | String | FK → Account | 關聯帳戶 ID |
| amount | BigDecimal | Required, > 0 | 扣款金額 |
| currency | String | Required, ISO 4217 | 幣別代碼 |
| status | TransactionStatus | Required | 交易狀態 |
| description | String | Optional, max 200 chars | 交易描述 |
| merchantId | String | Optional | 商家識別碼 |
| processedAt | Instant | Required | 處理時間 (UTC) |

**Validation Rules**:
- transactionId 格式必須為 `TXN-YYYYMMDD-XXX`
- amount 必須大於 0
- amount 不可超過帳戶餘額 (Precondition)
- 交易後餘額不可為負數 (Postcondition)

---

### TransactionStatus (交易狀態)

**Description**: 交易狀態枚舉。

| Value | Description |
|-------|-------------|
| SUCCESS | 交易成功 |
| FAILED | 交易失敗 |
| PENDING | 處理中 |

---

## Request/Response DTOs

### DebitRequest

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| transactionId | String | Required, Format: `TXN-YYYYMMDD-XXX` | 交易識別碼 |
| amount | BigDecimal | Required, > 0 | 扣款金額 |
| currency | String | Required | 幣別代碼 |
| description | String | Optional | 交易描述 |
| merchantId | String | Optional | 商家識別碼 |

### DebitResponse

| Field | Type | Description |
|-------|------|-------------|
| transactionId | String | 交易識別碼 |
| accountId | String | 帳戶識別碼 |
| status | String | 交易狀態 (SUCCESS/FAILED) |
| previousBalance | BigDecimal | 交易前餘額 |
| amount | BigDecimal | 扣款金額 |
| newBalance | BigDecimal | 交易後餘額 |
| processedAt | Instant | 處理時間 |

### ErrorResponse

| Field | Type | Description |
|-------|------|-------------|
| errorCode | String | 錯誤代碼 |
| message | String | 錯誤訊息 |
| timestamp | Instant | 錯誤發生時間 |
| path | String | 請求路徑 |
| details | Object | 額外錯誤細節 (Optional) |

---

## Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| ACCOUNT_NOT_FOUND | 404 | 帳戶不存在 |
| INSUFFICIENT_BALANCE | 400 | 餘額不足 |
| ACCOUNT_FROZEN | 400 | 帳戶已凍結 |
| ACCOUNT_CLOSED | 400 | 帳戶已關閉 |
| INVALID_AMOUNT | 400 | 金額無效 |
| INVALID_TRANSACTION_ID | 400 | 交易 ID 格式錯誤 |

---

## Data Volume Assumptions

| Entity | Expected Volume | Growth Rate |
|--------|-----------------|-------------|
| Account | 10,000 | 500/month |
| DebitTransaction | 100,000 | 10,000/month |

---

## Contract Data Examples

契約測試中使用的遮罩/虛擬資料：

| Field | Example Value | Note |
|-------|---------------|------|
| accountId | ACC-001, ACC-999 | 測試用帳戶 ID |
| accountHolder | 王大明 | 測試用姓名 |
| balance | 10000.00, 500.00 | 測試用餘額 |
| transactionId | TXN-20240115-001 | 測試用交易 ID |
| amount | 1000.00, 99999.00 | 測試用金額 |
