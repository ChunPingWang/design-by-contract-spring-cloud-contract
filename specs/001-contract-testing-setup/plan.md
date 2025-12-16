# Implementation Plan: Spring Cloud Contract 契約測試系統

**Branch**: `001-contract-testing-setup` | **Date**: 2025-12-16 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/001-contract-testing-setup/spec.md`

## Summary

實作 Spring Cloud Contract 契約測試系統，包含：
- **Provider (account-service)**: 定義帳戶服務 API 契約，自動產生驗證測試和 Stub
- **Consumer (payment-service)**: 使用 Stub Runner 執行契約測試，驗證與 Provider 的整合
- **CI/CD 整合**: Gitea Actions 自動執行契約測試、發布 Stub、阻止破壞性變更

技術方法採用 Design by Contract 原則，每個契約包含 Precondition、Postcondition 和 Invariant，確保服務間的一致性和可靠性。

## Technical Context

**Language/Version**: Java 17
**Primary Dependencies**: Spring Boot 3, Spring Cloud Contract 4.x, OpenFeign, Cucumber
**Storage**: H2 (測試用), 可擴展至 PostgreSQL
**Testing**: JUnit 5, Cucumber, AssertJ, Mockito, Rest Assured, Spring Cloud Contract Verifier
**Target Platform**: Linux Server (Gitea Actions runner)
**Project Type**: Multi-module (Provider + Consumer 微服務)
**Build Tool**: Gradle
**Performance Goals**: CI Pipeline 完整執行 ≤ 10 分鐘
**Constraints**: 契約測試失敗時阻止合併; 儲存庫連線重試 3 次
**Scale/Scope**: 2 微服務 (account-service, payment-service); 5+ API 契約

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原則 | 狀態 | 說明 |
|------|------|------|
| I. 程式碼品質 | ✅ PASS | 採用統一編碼風格、清晰命名、最小複雜度實作 |
| II. 測試優先 (TDD/BDD) | ✅ PASS | 契約先行定義 → 產生測試 → 實作通過; Given-When-Then 驗收條件; Cucumber BDD |
| III. 契約驅動開發 | ✅ PASS | 使用 Spring Cloud Contract Groovy DSL; 包含 Precondition/Postcondition/Invariant |
| IV. 領域驅動設計 | ✅ PASS | Account、DebitTransaction 為 Entity; 狀態機定義清晰 |
| V. SOLID 原則 | ✅ PASS | 單一職責 (Controller/Service/Repository 分離); 依賴反轉 (透過介面) |
| VI. 六角形架構 | ✅ PASS | Domain 層無框架依賴; Infrastructure 層處理 Spring/JPA |

### 契約規範檢查

| 規範 | 狀態 | 說明 |
|------|------|------|
| 契約目錄 | ✅ PASS | `src/test/resources/contracts/` |
| 命名格式 | ✅ PASS | `動詞+名詞.groovy` (如 getAccount.groovy) |
| 必要區塊 | ✅ PASS | name, description, request, response |
| DbC 說明 | ✅ PASS | description 包含 Precondition/Postcondition/Invariant |

## Project Structure

### Documentation (this feature)

```text
specs/001-contract-testing-setup/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── openapi.yaml     # OpenAPI 3.0 specification
└── tasks.md             # Phase 2 output (/speckit.tasks command)
```

### Source Code (repository root)

```text
account-service/                    # Provider 微服務
├── src/
│   ├── main/java/com/example/account/
│   │   ├── domain/                 # Domain Layer (無框架依賴)
│   │   │   ├── Account.java
│   │   │   ├── AccountStatus.java
│   │   │   ├── DebitRequest.java
│   │   │   └── DebitResponse.java
│   │   ├── application/            # Application Layer
│   │   │   └── AccountService.java
│   │   ├── infrastructure/         # Infrastructure Layer
│   │   │   ├── controller/
│   │   │   │   └── AccountController.java
│   │   │   ├── repository/
│   │   │   │   └── AccountRepository.java
│   │   │   └── exception/
│   │   │       ├── AccountNotFoundException.java
│   │   │       └── InsufficientBalanceException.java
│   │   └── AccountServiceApplication.java
│   └── main/resources/
│       └── application.yml
├── src/test/
│   ├── java/com/example/account/
│   │   ├── ContractVerifierBase.java
│   │   └── cucumber/               # Cucumber BDD 測試
│   │       └── AccountSteps.java
│   └── resources/
│       ├── contracts/
│       │   └── account/
│       │       ├── getAccount.groovy
│       │       ├── getAccountNotFound.groovy
│       │       ├── createAccount.groovy
│       │       ├── debitAccount.groovy
│       │       └── debitInsufficientBalance.groovy
│       └── features/               # Cucumber feature files
│           └── account.feature
├── build.gradle
└── Dockerfile

payment-service/                    # Consumer 微服務
├── src/
│   ├── main/java/com/example/payment/
│   │   ├── domain/
│   │   │   └── PaymentRequest.java
│   │   ├── application/
│   │   │   └── PaymentService.java
│   │   ├── infrastructure/
│   │   │   ├── controller/
│   │   │   │   └── PaymentController.java
│   │   │   └── client/
│   │   │       ├── AccountClient.java
│   │   │       └── dto/
│   │   └── PaymentServiceApplication.java
│   └── main/resources/
│       └── application.yml
├── src/test/
│   ├── java/com/example/payment/
│   │   ├── contract/
│   │   │   └── AccountClientContractTest.java
│   │   └── cucumber/
│   │       └── PaymentSteps.java
│   └── resources/
│       ├── application-contract-test.yml
│       └── features/
│           └── payment.feature
├── build.gradle
└── Dockerfile

build.gradle                        # Root Gradle build file
settings.gradle                     # Multi-module settings

.gitea/workflows/                   # CI/CD 配置
├── account-service-ci.yaml
├── payment-service-ci.yaml
└── contract-verify.yaml
```

**Structure Decision**: 採用多專案結構，每個微服務獨立模組，遵循六角形架構分層。使用 Gradle 作為建置工具，支援 multi-project build。Provider 和 Consumer 各自有獨立的 CI Pipeline。

## Complexity Tracking

> 無違規需要說明。所有設計決策符合 Constitution 原則。
