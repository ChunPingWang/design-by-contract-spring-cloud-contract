# Design by Contract with Spring Cloud Contract

> 使用 Spring Cloud Contract 實現契約驅動開發的教學專案

---

## Speckit 工作流程指令

本專案使用 [Speckit](https://github.com/speckit) 進行 AI 輔助的規格驅動開發。以下是完整的工作流程指令：

### 初始化

```bash
# 初始化 Speckit 專案
# --ai 參數選項: copilot (GitHub Copilot), claude (Claude), gemini (Gemini)
specify init --ai copilot --script sh --here
```

### 規格開發流程

在 AI Chat 介面中依序執行以下指令：

| 步驟 | 指令 | 說明 |
|------|------|------|
| 1 | `/speckit.constitution @Constitution.md` | **建立專案準則** - 定義開發規範、程式碼品質標準、測試要求等專案憲法 |
| 2 | `/speckit.specify @spring-cloud-contract-spec.md` | **建立功能規格** - 根據技術規格文件產生結構化的功能規格 (spec.md) |
| 3 | `/speckit.clarify` | **釐清需求** - AI 提問釐清模糊需求，將答案編碼回規格中 |
| 4 | `/speckit.plan @Tech.md` | **建立實作計畫** - 根據技術選型產生詳細的實作計畫 (plan.md) |
| 5 | `/speckit.tasks` | **產生任務清單** - 產生依賴排序的可執行任務清單 (tasks.md) |
| 6 | `/speckit.analyze` | **分析一致性** - 交叉分析 spec.md、plan.md、tasks.md 的一致性與品質，結果儲存到 `<feature>.md` |
| 7 | `/speckit.implement` | **執行實作** - 根據 tasks.md 中的任務清單，逐一執行實作 |

### 工作流程圖

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Constitution   │────▶│    Specify      │────▶│    Clarify      │
│  (專案準則)      │     │  (功能規格)      │     │  (需求釐清)      │
└─────────────────┘     └─────────────────┘     └────────┬────────┘
                                                         │
                                                         ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Analyze      │◀────│     Tasks       │◀────│      Plan       │
│  (一致性分析)    │     │   (任務清單)     │     │   (實作計畫)     │
└────────┬────────┘     └─────────────────┘     └─────────────────┘
         │
         ▼
┌─────────────────┐
│   Implement     │
│   (執行實作)     │
└─────────────────┘
```

### 產出檔案

執行上述流程後，會在 `specs/<feature-branch>/` 目錄下產生：

| 檔案 | 說明 |
|------|------|
| `spec.md` | 功能規格書 - 包含 User Stories、Acceptance Criteria、Requirements |
| `plan.md` | 實作計畫 - 包含架構設計、技術決策、實作步驟 |
| `tasks.md` | 任務清單 - 依賴排序的可執行任務 |
| `<feature>.md` | 分析報告 - 交叉分析結果與建議 |

---

## 專案狀態

| 項目 | 狀態 |
|------|------|
| 分支 | `001-contract-testing-setup` |
| 階段 | 基礎架構建置完成 |
| Provider | account-service (帳戶服務) |
| Consumer | payment-service (支付服務) |

## 技術棧

- **Java 17** - 主要開發語言
- **Spring Boot 3.2** - 應用框架
- **Spring Cloud Contract 4.x** - 契約測試框架
- **Gradle** - 建置工具
- **JUnit 5** - 單元測試框架
- **Cucumber** - BDD 測試框架
- **H2** - 測試用記憶體資料庫
- **OpenFeign** - 宣告式 HTTP 客戶端

---

## 目錄

1. [Speckit 工作流程指令](#speckit-工作流程指令)
2. [Design by Contract 概念](#design-by-contract-概念)
3. [六角形架構 (Hexagonal Architecture)](#六角形架構-hexagonal-architecture)
4. [Spring Cloud Contract 介紹](#spring-cloud-contract-介紹)
5. [專案結構](#專案結構)
6. [快速開始](#快速開始)
7. [契約定義教學](#契約定義教學)
8. [測試執行](#測試執行)

---

## Design by Contract 概念

Design by Contract (DbC) 是一種軟體設計方法，透過定義明確的「契約」來規範軟體元件之間的互動。

### 三大核心元素

```
┌─────────────────────────────────────────────────────────────┐
│                    Design by Contract                        │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Precondition (前置條件)                             │    │
│  │  呼叫方必須滿足的條件，才能正確執行操作               │    │
│  │  例：amount > 0, 帳戶狀態為 ACTIVE                   │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│                           ▼                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Operation (操作執行)                                │    │
│  │  實際的業務邏輯執行                                  │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│                           ▼                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Postcondition (後置條件)                            │    │
│  │  操作完成後保證的結果狀態                            │    │
│  │  例：newBalance = oldBalance - amount               │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  Invariant (不變量)                                  │    │
│  │  無論何時都必須維持的條件                            │    │
│  │  例：balance >= 0                                    │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### 在 API 中的應用

以帳戶扣款 API 為例：

| 契約元素 | 說明 | 範例 |
|---------|------|------|
| **Precondition** | 呼叫前必須滿足 | `amount > 0`, `amount <= balance`, `status == ACTIVE` |
| **Postcondition** | 執行後保證 | `newBalance = oldBalance - amount`, 返回交易記錄 |
| **Invariant** | 始終成立 | `balance >= 0` |

### 為什麼使用 DbC？

1. **明確責任邊界** - 清楚定義呼叫方與被呼叫方的責任
2. **提早發現問題** - 在開發階段而非生產環境發現整合問題
3. **文件即程式碼** - 契約本身就是最新、最準確的文件
4. **降低整合成本** - 減少服務間整合測試的複雜度

---

## 六角形架構 (Hexagonal Architecture)

本專案採用六角形架構（又稱 Ports and Adapters），確保業務邏輯與技術細節分離。

### 架構圖

```
                    ┌─────────────────────────────────────────┐
                    │           Infrastructure Layer          │
                    │  (框架、資料庫、外部服務 - 可替換)        │
                    │                                         │
                    │    ┌─────────────────────────────┐      │
                    │    │      Application Layer      │      │
                    │    │   (Use Cases / Services)    │      │
                    │    │                             │      │
                    │    │   ┌─────────────────────┐   │      │
                    │    │   │    Domain Layer     │   │      │
                    │    │   │  (核心業務邏輯)      │   │      │
                    │    │   │  - Entities         │   │      │
                    │    │   │  - Value Objects    │   │      │
                    │    │   │  - Domain Services  │   │      │
                    │    │   └─────────────────────┘   │      │
                    │    │                             │      │
                    │    └─────────────────────────────┘      │
                    │                                         │
                    └─────────────────────────────────────────┘
```

### 各層說明

#### Domain Layer (領域層) - 最內層

```java
// 位置: src/main/java/com/example/account/domain/

├── Account.java          // 實體 - 帳戶核心業務邏輯
├── AccountStatus.java    // 值物件 - 帳戶狀態枚舉
├── DebitRequest.java     // 值物件 - 扣款請求
└── DebitResponse.java    // 值物件 - 扣款回應
```

- **職責**：包含純粹的業務邏輯，不依賴任何框架
- **特點**：可獨立測試，不需要 Spring 容器
- **原則**：不引入任何 Infrastructure 依賴

#### Application Layer (應用層) - 中間層

```java
// 位置: src/main/java/com/example/account/application/

├── AccountService.java       // 介面 - 定義用例
└── AccountServiceImpl.java   // 實作 - 協調領域物件
```

- **職責**：實現用例 (Use Cases)，協調領域物件完成業務流程
- **特點**：定義介面，讓外層實作注入
- **原則**：只依賴 Domain Layer

#### Infrastructure Layer (基礎設施層) - 最外層

```java
// 位置: src/main/java/com/example/account/infrastructure/

├── controller/
│   └── AccountController.java    // REST API 端點
├── repository/
│   └── AccountRepository.java    // 資料存取
├── dto/
│   ├── CreateAccountRequest.java
│   ├── FreezeAccountRequest.java
│   └── ErrorResponse.java
└── exception/
    ├── AccountNotFoundException.java
    ├── AccountFrozenException.java
    ├── InsufficientBalanceException.java
    └── GlobalExceptionHandler.java
```

- **職責**：處理外部技術細節（HTTP、資料庫、訊息佇列等）
- **特點**：可替換，例如從 H2 換成 PostgreSQL 不影響業務邏輯
- **原則**：依賴內層，實作內層定義的介面

### SOLID 原則應用

| 原則 | 在本專案的應用 |
|------|---------------|
| **S**ingle Responsibility | 每個類別只有一個變更理由 |
| **O**pen/Closed | 透過介面擴展，不修改既有程式碼 |
| **L**iskov Substitution | Repository 實作可替換 |
| **I**nterface Segregation | 小而專一的介面設計 |
| **D**ependency Inversion | 高層模組不依賴低層模組，都依賴抽象 |

---

## Spring Cloud Contract 介紹

Spring Cloud Contract 是一個支援 Consumer Driven Contracts 的測試框架，讓 Provider 和 Consumer 能基於共同的契約進行獨立開發和測試。

### 工作流程

```
┌──────────────────────────────────────────────────────────────┐
│                 Spring Cloud Contract 工作流程                │
│                                                               │
│  Phase 1: Contract Definition (Provider 端)                  │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  contracts/                                          │     │
│  │  └── account/                                        │     │
│  │      ├── getAccount.groovy      ← GET /accounts/{id} │     │
│  │      ├── createAccount.groovy   ← POST /accounts     │     │
│  │      └── debitAccount.groovy    ← POST /debit        │     │
│  └─────────────────────────────────────────────────────┘     │
│                              │                                │
│                              ▼                                │
│  Phase 2: Auto-Generate (Gradle Plugin)                      │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  ./gradlew generateContractTests                     │     │
│  │                              │                        │     │
│  │              ┌───────────────┴───────────────┐       │     │
│  │              ▼                               ▼       │     │
│  │  ┌─────────────────────┐      ┌─────────────────┐   │     │
│  │  │ Generated Tests     │      │ Stub JAR        │   │     │
│  │  │ (驗證 Provider)     │      │ (給 Consumer)   │   │     │
│  │  └─────────────────────┘      └─────────────────┘   │     │
│  └─────────────────────────────────────────────────────┘     │
│                                                               │
│  Phase 3: Consumer Integration                               │
│  ┌─────────────────────────────────────────────────────┐     │
│  │  @AutoConfigureStubRunner(                          │     │
│  │      ids = "com.example:account-service:+:stubs"    │     │
│  │  )                                                  │     │
│  │  class PaymentServiceContractTest { ... }           │     │
│  └─────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```

### Provider vs Consumer

| 角色 | 說明 | 本專案範例 |
|------|------|-----------|
| **Provider** | 提供 API 的服務 | account-service (帳戶服務) |
| **Consumer** | 呼叫 API 的服務 | payment-service (支付服務) |

### 核心元件

1. **Contract DSL** - 用 Groovy 定義契約
2. **Contract Verifier** - 從契約產生測試，驗證 Provider
3. **Stub Runner** - Consumer 端使用，自動啟動 Mock 服務

---

## 專案結構

```
design-by-contract-spring-cloud-contract/
├── account-service/                 # Provider - 帳戶服務
│   ├── src/
│   │   ├── main/java/com/example/account/
│   │   │   ├── AccountServiceApplication.java
│   │   │   ├── domain/              # 領域層
│   │   │   │   ├── Account.java
│   │   │   │   ├── AccountStatus.java
│   │   │   │   ├── DebitRequest.java
│   │   │   │   └── DebitResponse.java
│   │   │   ├── application/         # 應用層
│   │   │   │   ├── AccountService.java
│   │   │   │   └── AccountServiceImpl.java
│   │   │   └── infrastructure/      # 基礎設施層
│   │   │       ├── controller/
│   │   │       ├── repository/
│   │   │       ├── dto/
│   │   │       └── exception/
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle
│
├── payment-service/                 # Consumer - 支付服務
│   ├── src/
│   │   ├── main/java/com/example/payment/
│   │   │   └── PaymentServiceApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── build.gradle (待建立)
│
├── specs/                           # 規格文件
│   └── 001-contract-testing-setup/
│       ├── spec.md                  # 功能規格
│       ├── plan.md                  # 實作計畫
│       └── tasks.md                 # 任務清單
│
├── build.gradle                     # 根專案建置設定
├── settings.gradle                  # 多模組設定
├── Constitution.md                  # 專案準則
├── Tech.md                          # 技術選型
└── README.md                        # 本文件
```

---

## 快速開始

### 前置需求

- Java 17+
- Gradle 8.x (使用 wrapper)

### 建置專案

```bash
# 建置整個專案
./gradlew build

# 只建置 account-service
./gradlew :account-service:build
```

### 執行契約測試

```bash
# Provider 端 - 產生契約測試並執行
./gradlew :account-service:contractTest

# Provider 端 - 產生 Stub JAR
./gradlew :account-service:verifierStubsJar
```

### 啟動服務

```bash
# 啟動帳戶服務
./gradlew :account-service:bootRun
```

---

## 契約定義教學

### 基本契約結構

契約檔案位於 `src/test/resources/contracts/` 目錄下。

```groovy
// getAccount.groovy
package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "查詢帳戶 - 成功"
    description """
        Contract (Design by Contract):
        - Precondition: accountId 格式為 ACC-XXX，帳戶存在
        - Postcondition: 返回帳戶資訊，包含 accountId, balance, status
        - Invariant: balance >= 0
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-001"
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            accountId: "ACC-001",
            accountHolder: "王大明",
            balance: 10000.00,
            currency: "TWD",
            status: "ACTIVE"
        ])
    }
}
```

### 常用 Contract DSL 語法

| 語法 | 說明 | 範例 |
|------|------|------|
| `$(consumer(...), producer(...))` | 分別定義 Consumer/Provider 值 | `$(consumer("test"), producer(regex(".*")))` |
| `anyDouble()` | 任意浮點數 | `balance: $(producer(anyDouble()))` |
| `regex(...)` | 正則表達式匹配 | `$(producer(regex("[A-Z]{3}")))` |
| `optional(...)` | 可選欄位 | `description: $(consumer(optional("desc")))` |
| `fromRequest()` | 引用 Request 值 | `transactionId: fromRequest().body('$.transactionId')` |

### 錯誤場景契約

```groovy
// getAccountNotFound.groovy
Contract.make {
    name "查詢帳戶 - 不存在"
    description """
        Contract:
        - Precondition: accountId 格式正確但帳戶不存在
        - Postcondition: 返回 404 及錯誤訊息
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-999"
    }

    response {
        status NOT_FOUND()
        body([
            errorCode: "ACCOUNT_NOT_FOUND",
            message: "帳戶不存在"
        ])
    }
}
```

---

## 測試執行

### Provider 端測試

需要建立 Base Class 來設定測試環境：

```java
// ContractVerifierBase.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("contract-test")
public abstract class ContractVerifierBase {

    @Autowired
    private AccountController accountController;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(accountController);
        // 設定 Mock 行為...
    }
}
```

### Consumer 端測試

使用 `@AutoConfigureStubRunner` 自動啟動 Stub：

```java
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class AccountClientContractTest {

    @Autowired
    private AccountClient accountClient;

    @Test
    void shouldGetAccountSuccessfully() {
        AccountResponse response = accountClient.getAccount("ACC-001");

        assertThat(response.getAccountId()).isEqualTo("ACC-001");
        assertThat(response.getBalance()).isNotNull();
    }
}
```

---

## 帳戶狀態機

帳戶狀態遵循以下狀態轉換規則：

```
    ┌─────────┐
    │ PENDING │ ──────────────────┐
    └────┬────┘                   │
         │ activate()             │
         ▼                        │
    ┌─────────┐     freeze()      │
    │ ACTIVE  │ ◄────────────┐    │
    └────┬────┘              │    │
         │ freeze()          │    │
         ▼                   │    │
    ┌─────────┐   unfreeze() │    │
    │ FROZEN  │ ─────────────┘    │
    └────┬────┘                   │
         │ close()                │
         ▼                        │
    ┌─────────┐                   │
    │ CLOSED  │ ◄─────────────────┘
    └─────────┘       close()
```

| 狀態 | 說明 | 可執行操作 |
|------|------|-----------|
| PENDING | 待啟用 | 啟用、關閉 |
| ACTIVE | 啟用中 | 扣款、凍結、關閉 |
| FROZEN | 已凍結 | 解凍、關閉 |
| CLOSED | 已關閉 | 無 |

---

## 參考資源

- [Spring Cloud Contract 官方文件](https://spring.io/projects/spring-cloud-contract)
- [Contract DSL 參考](https://cloud.spring.io/spring-cloud-contract/reference/html/project-features.html#contract-dsl)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Design by Contract - Bertrand Meyer](https://en.wikipedia.org/wiki/Design_by_contract)

---

## 授權

本專案僅供學習用途。
