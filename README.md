# Design by Contract with Spring Cloud Contract

> 使用 Spring Cloud Contract 實現契約驅動開發的教學專案

本專案展示如何在微服務架構中應用 **Design by Contract (DbC)** 設計原則，並透過 **Spring Cloud Contract** 實現 Provider 與 Consumer 之間的契約測試，確保服務間的整合正確性。

---

## 專案簡介

### 背景與動機

在微服務架構中，服務間的通訊是一個常見的挑戰。傳統的整合測試需要同時啟動多個服務，成本高且難以維護。**契約測試 (Contract Testing)** 提供了一種更輕量級的解決方案：

- **Provider** 定義 API 契約，承諾提供特定格式的回應
- **Consumer** 基於契約產生的 Stub 進行測試，無需啟動真實的 Provider
- 契約作為 Provider 與 Consumer 之間的「合約」，確保雙方的期望一致

### 本專案的目標

1. 示範如何使用 **Spring Cloud Contract** 實現契約驅動開發
2. 展示 **Design by Contract** 三大核心元素在 API 設計中的應用
3. 提供完整的 **Provider/Consumer** 契約測試範例
4. 整合 **Cucumber BDD** 進行行為驅動開發測試

### 專案架構

```
┌─────────────────────────────────────────────────────────────────────┐
│                          契約測試流程                                 │
│                                                                      │
│   ┌──────────────────┐         契約 (Groovy DSL)         ┌─────────────────┐
│   │                  │  ─────────────────────────────▶  │                 │
│   │  account-service │                                   │ payment-service │
│   │    (Provider)    │  ◀─────────────────────────────  │   (Consumer)    │
│   │                  │         Stub JAR (WireMock)       │                 │
│   └──────────────────┘                                   └─────────────────┘
│           │                                                      │
│           │                                                      │
│           ▼                                                      ▼
│   ┌──────────────────┐                               ┌─────────────────┐
│   │ Contract Verifier │                               │   Stub Runner   │
│   │  自動產生測試驗證  │                               │  自動啟動 Mock  │
│   │  Provider 實作    │                               │  服務供測試用    │
│   └──────────────────┘                               └─────────────────┘
└─────────────────────────────────────────────────────────────────────┘
```

### 服務說明

| 服務 | 角色 | 說明 |
|------|------|------|
| **account-service** | Provider | 帳戶服務 - 提供帳戶查詢、建立、扣款、凍結/解凍等 API |
| **payment-service** | Consumer | 支付服務 - 呼叫帳戶服務進行支付交易 |

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
| 階段 | ✅ 完整實作完成 |
| Provider | account-service (帳戶服務) |
| Consumer | payment-service (支付服務) |

### 測試涵蓋率

| 服務 | 涵蓋率 | 狀態 |
|------|--------|------|
| account-service | **97%** | ✅ 通過 |
| payment-service | **100%** | ✅ 通過 |

### 測試統計

| 類型 | account-service | payment-service |
|------|-----------------|-----------------|
| 單元測試 | 59 個 | 17 個 |
| 契約測試 | 8 個 | 4 個 |
| Cucumber BDD | 5 個場景 | 2 個場景 |

### 涵蓋率細項

**account-service:**
| 套件 | 涵蓋率 |
|------|--------|
| domain | 100% |
| application | 93% |
| controller | 99% |

**payment-service:**
| 套件 | 涵蓋率 |
|------|--------|
| domain | 100% |
| application | 100% |
| controller | 100% |

## 技術棧

- **Java 17** - 主要開發語言
- **Spring Boot 3.2** - 應用框架
- **Spring Cloud Contract 4.x** - 契約測試框架
- **Gradle 8.x** - 建置工具
- **JUnit 5** - 單元測試框架
- **Cucumber 7.x** - BDD 測試框架
- **JaCoCo** - 測試涵蓋率報告
- **H2** - 測試用記憶體資料庫
- **OpenFeign** - 宣告式 HTTP 客戶端
- **Micrometer + Prometheus** - 監控指標
- **Logback + JSON** - 結構化日誌

---

## 目錄

1. [專案簡介](#專案簡介)
2. [Speckit 工作流程指令](#speckit-工作流程指令)
3. [Design by Contract 概念](#design-by-contract-概念)
4. [六角形架構 (Hexagonal Architecture)](#六角形架構-hexagonal-architecture)
5. [Spring Cloud Contract 介紹](#spring-cloud-contract-介紹)
6. [WireMock 與 Stub Runner](#wiremock-與-stub-runner)
7. [專案結構](#專案結構)
8. [快速開始](#快速開始)
9. [契約定義教學](#契約定義教學)
10. [測試執行](#測試執行)

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

## WireMock 與 Stub Runner

### 什麼是 WireMock？

**WireMock** 是一個用於模擬 HTTP 服務的工具，Spring Cloud Contract 使用它來產生和執行 Stub。當 Provider 定義契約後，會自動產生 WireMock 格式的 JSON 映射檔，讓 Consumer 可以在不啟動真實 Provider 的情況下進行測試。

### Stub JAR 的產生與使用

```
┌─────────────────────────────────────────────────────────────────┐
│                        Stub JAR 生命週期                         │
│                                                                  │
│  1. Provider 定義契約                                            │
│     account-service/src/test/resources/contracts/               │
│     └── account/                                                │
│         └── getAccount.groovy                                   │
│                                                                  │
│  2. 建置產生 Stub JAR                                            │
│     ./gradlew :account-service:verifierStubsJar                 │
│     └── build/libs/account-service-1.0.0-SNAPSHOT-stubs.jar    │
│                                                                  │
│  3. 發布到 Maven 倉庫                                            │
│     ./gradlew :account-service:publishToMavenLocal              │
│     └── ~/.m2/repository/com/example/account-service/           │
│                                                                  │
│  4. Consumer 使用 Stub                                           │
│     @AutoConfigureStubRunner(                                   │
│         ids = "com.example:account-service:+:stubs:6565"        │
│     )                                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Stub JAR 內容結構

```
account-service-1.0.0-SNAPSHOT-stubs.jar
├── META-INF/
│   └── com.example/
│       └── account-service/
│           └── 1.0.0-SNAPSHOT/
│               └── contracts/
│                   └── account/
│                       ├── getAccount.groovy
│                       ├── getAccountNotFound.groovy
│                       └── ...
└── mappings/
    └── account/
        ├── getAccount.json          ← WireMock 映射
        ├── getAccountNotFound.json
        └── ...
```

### WireMock 映射檔範例

從契約自動產生的 WireMock JSON 映射：

```json
{
  "id": "get_account_success",
  "request": {
    "method": "GET",
    "urlPath": "/api/v1/accounts/ACC-001"
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/json"
    },
    "jsonBody": {
      "accountNumber": "ACC-001",
      "ownerName": "王大明",
      "balance": 10000.00,
      "status": "ACTIVE"
    }
  }
}
```

### Stub Runner 配置

#### 方式一：使用 @AutoConfigureStubRunner 註解

```java
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = "account-service.url=http://localhost:6565"
)
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:6565",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class AccountClientContractTest {

    @Autowired
    private AccountClient accountClient;

    @Test
    void shouldGetAccountSuccessfully() {
        AccountDto account = accountClient.getAccount("ACC-001");

        assertThat(account.accountNumber()).isEqualTo("ACC-001");
        assertThat(account.ownerName()).isEqualTo("王大明");
    }
}
```

#### 參數說明

| 參數 | 說明 | 範例 |
|------|------|------|
| `ids` | Stub 座標 (groupId:artifactId:version:classifier:port) | `com.example:account-service:+:stubs:6565` |
| `stubsMode` | Stub 來源模式 | `LOCAL` (本地 Maven), `REMOTE` (遠端倉庫), `CLASSPATH` |
| `repositoryRoot` | Maven 倉庫 URL | `stubs://file://~/.m2/repository` |

#### 方式二：使用 application.yml 配置

```yaml
# application-contract-test.yml
stubrunner:
  ids:
    - com.example:account-service:+:stubs:6565
  stubs-mode: LOCAL

account-service:
  url: http://localhost:6565
```

### 常見問題與解決方案

#### 1. Stub 找不到

```
Could not find stub for com.example:account-service:+:stubs
```

**解決方案：**
```bash
# 確認已發布 Stub 到本地 Maven
./gradlew :account-service:publishToMavenLocal

# 檢查 Stub JAR 是否存在
ls ~/.m2/repository/com/example/account-service/*/
```

#### 2. Port 衝突

```
Port 6565 is already in use
```

**解決方案：** 變更 Stub Runner 埠號
```java
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:8090"  // 使用其他埠號
)
```

#### 3. Feign Client URL 不匹配

**解決方案：** 確保 Feign Client URL 與 Stub Runner 埠號一致
```java
@SpringBootTest(
    properties = "account-service.url=http://localhost:6565"
)
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:6565"
)
```

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
│   │       ├── application.yml
│   │       └── logback-spring.xml   # JSON 結構化日誌
│   ├── src/test/
│   │   ├── java/com/example/account/
│   │   │   ├── ContractVerifierBase.java
│   │   │   ├── domain/              # 領域層單元測試
│   │   │   ├── infrastructure/controller/
│   │   │   └── cucumber/            # BDD 測試
│   │   └── resources/
│   │       ├── contracts/account/   # Groovy 契約
│   │       └── features/            # Cucumber 特性檔
│   ├── build.gradle
│   └── Dockerfile
│
├── payment-service/                 # Consumer - 支付服務
│   ├── src/
│   │   ├── main/java/com/example/payment/
│   │   │   ├── PaymentServiceApplication.java
│   │   │   ├── domain/
│   │   │   ├── application/
│   │   │   └── infrastructure/
│   │   │       ├── controller/
│   │   │       └── client/          # Feign 客戶端
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   ├── src/test/
│   │   ├── java/com/example/payment/
│   │   │   ├── contract/            # Consumer 契約測試
│   │   │   ├── application/
│   │   │   ├── infrastructure/controller/
│   │   │   └── cucumber/
│   │   └── resources/features/
│   ├── build.gradle
│   └── Dockerfile
│
├── docs/                            # 文件
│   ├── contract-versioning.md       # 契約版本管理
│   ├── ci-cd-setup.md               # CI/CD 設定
│   └── breaking-change-detection.md # 破壞性變更偵測
│
├── .github/workflows/               # CI/CD 工作流程
│   ├── account-service-ci.yaml
│   ├── payment-service-ci.yaml
│   └── contract-verify.yaml
│
├── specs/                           # 規格文件
│   └── 001-contract-testing-setup/
│       ├── spec.md
│       ├── plan.md
│       ├── tasks.md
│       └── quickstart.md
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
# 建置整個專案（含測試與涵蓋率驗證）
./gradlew clean build

# 只建置 account-service
./gradlew :account-service:build

# 只建置 payment-service
./gradlew :payment-service:build
```

### 執行契約測試

```bash
# Provider 端 - 產生契約測試並執行
./gradlew :account-service:contractTest

# Provider 端 - 產生 Stub JAR
./gradlew :account-service:verifierStubsJar

# 發布 Stub 到本地 Maven 倉庫
./gradlew :account-service:publishToMavenLocal

# Consumer 端 - 使用 Stub 執行契約測試
./gradlew :payment-service:test
```

### 執行測試涵蓋率報告

```bash
# 產生涵蓋率報告
./gradlew test jacocoTestReport

# 報告位置:
# - account-service/build/reports/jacoco/test/html/index.html
# - payment-service/build/reports/jacoco/test/html/index.html

# 驗證涵蓋率門檻（70%）
./gradlew jacocoTestCoverageVerification
```

### 執行 Cucumber BDD 測試

```bash
# 執行所有 Cucumber 測試
./gradlew test --tests "*Cucumber*"
```

### 啟動服務

```bash
# 啟動帳戶服務
./gradlew :account-service:bootRun

# 啟動支付服務
./gradlew :payment-service:bootRun
```

### Docker 部署

```bash
# 建置 Docker 映像
docker build -t account-service:latest ./account-service
docker build -t payment-service:latest ./payment-service

# 執行容器
docker run -p 8080:8080 account-service:latest
docker run -p 8081:8081 -e ACCOUNT_SERVICE_URL=http://host.docker.internal:8080 payment-service:latest
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
