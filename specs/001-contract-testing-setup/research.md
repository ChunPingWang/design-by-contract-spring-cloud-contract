# Research: Spring Cloud Contract 契約測試系統

**Feature**: 001-contract-testing-setup
**Date**: 2025-12-16

## 1. Spring Cloud Contract 最佳實踐

### Decision
採用 Spring Cloud Contract 4.x 作為契約測試框架，使用 Groovy DSL 定義契約，搭配 Gradle 建置工具。

### Rationale
- Spring Cloud Contract 與 Spring Boot 生態系完美整合
- Groovy DSL 提供靈活的契約定義能力，支援正則表達式和 Matcher
- 自動產生 Provider 端驗證測試和 Consumer 端 Stub
- 支援 Gradle 建置工具整合

### Alternatives Considered
| 替代方案 | 優點 | 缺點 | 為何不採用 |
|---------|------|------|-----------|
| Pact | 語言無關、Pact Broker 集中管理 | 需額外架設 Pact Broker、學習曲線較陡 | 專案已採用 Spring 生態系，SCC 整合更無縫 |
| OpenAPI Contract Testing | 標準化、工具豐富 | 無法自動產生測試、缺乏 DbC 語義 | 無法滿足 Precondition/Postcondition 需求 |
| 手動 Mock | 簡單直接 | 維護成本高、契約無法同步 | 不符合 Constitution 測試優先原則 |

---

## 2. Gradle 建置工具配置

### Decision
採用 Gradle 作為建置工具，使用 Kotlin DSL 或 Groovy DSL 定義建置腳本。

### Rationale
- Tech.md 明確指定使用 Gradle
- Gradle 提供增量建置，建置速度較快
- 支援 multi-project build，適合微服務架構
- Spring Cloud Contract Gradle Plugin 提供完整支援

### Gradle Configuration Patterns

**Root build.gradle**:
```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0' apply false
    id 'io.spring.dependency-management' version '1.1.4' apply false
}

allprojects {
    group = 'com.example'
    version = '1.0.0-SNAPSHOT'
}

subprojects {
    apply plugin: 'java'

    java {
        sourceCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }
}
```

**Provider build.gradle (account-service)**:
```groovy
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'org.springframework.cloud.contract' version '4.1.0'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
}

contracts {
    testFramework = TestFramework.JUNIT5
    baseClassForTests = 'com.example.account.ContractVerifierBase'
}
```

**Consumer build.gradle (payment-service)**:
```groovy
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-stub-runner'
}
```

---

## 3. 六角形架構在微服務中的應用

### Decision
Provider 和 Consumer 均採用三層六角形架構：Domain → Application → Infrastructure。

### Rationale
- **Domain Layer**: 純業務邏輯，無框架依賴，易於單元測試
- **Application Layer**: 用例編排，協調 Domain 和 Infrastructure
- **Infrastructure Layer**: 框架相關程式碼（Spring MVC、JPA、Feign）

### Layer Responsibilities

| 層級 | 職責 | 典型類別 |
|------|------|---------|
| Domain | 業務規則、Entity、Value Object | Account, AccountStatus, DebitRequest |
| Application | 用例協調、交易邊界 | AccountService, PaymentService |
| Infrastructure | 外部整合、框架適配 | AccountController, AccountRepository, AccountClient |

### Dependency Rules
```
Infrastructure → Application → Domain
       ↓              ↓           ↓
   Spring MVC     Use Cases    Pure Java
   JPA/Feign      DTOs         Entities
   REST API       Orchestration Business Logic
```

---

## 4. Contract DSL 設計模式

### Decision
契約採用 Design by Contract 原則，每個契約包含完整的 Precondition、Postcondition 和 Invariant 說明。

### Rationale
- 明確定義 API 的輸入條件和輸出保證
- 提供可執行的文件，契約即規格
- 便於追蹤和驗證業務不變量

### Contract Structure Pattern
```groovy
Contract.make {
    name "動詞+名詞 - 場景"
    description """
        Contract (Design by Contract):
        - Precondition: [輸入條件]
        - Postcondition: [輸出保證]
        - Invariant: [業務不變量]
    """

    request {
        method [HTTP_METHOD]()
        url "[endpoint]"
        headers { /* ... */ }
        body([ /* ... */ ])
    }

    response {
        status [STATUS_CODE]()
        headers { /* ... */ }
        body([ /* ... */ ])
        bodyMatchers { /* ... */ }
    }
}
```

---

## 5. Stub Runner 配置策略

### Decision
Consumer 測試使用 `@AutoConfigureStubRunner` 自動下載和啟動 Stub，支援 LOCAL 和 REMOTE 模式。

### Rationale
- LOCAL 模式：開發階段快速迭代
- REMOTE 模式：CI 環境從 Artifact Repository 下載最新 Stub

### Configuration Patterns

**Local Mode (開發)**:
```java
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
```

**Remote Mode (CI)**:
```java
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.REMOTE,
    repositoryRoot = "${NEXUS_URL}/repository/maven-snapshots/"
)
```

---

## 6. Cucumber BDD 整合

### Decision
採用 Cucumber 作為 BDD 測試框架，與 Spring Cloud Contract 並行使用。

### Rationale
- Tech.md 明確指定使用 Cucumber
- Cucumber 提供 Given-When-Then 自然語言描述
- 符合 Constitution 的 BDD 要求
- 便於與非技術人員溝通驗收條件

### Cucumber Configuration

**build.gradle**:
```groovy
dependencies {
    testImplementation 'io.cucumber:cucumber-java:7.14.0'
    testImplementation 'io.cucumber:cucumber-spring:7.14.0'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine:7.14.0'
}

tasks.named('test') {
    useJUnitPlatform()
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
}
```

**Feature File Example**:
```gherkin
Feature: Account Management
  As an account holder
  I want to manage my account
  So that I can track my balance

  Scenario: Query existing account
    Given an account with ID "ACC-001" exists with balance 10000.00
    When I query the account "ACC-001"
    Then I should receive account details with balance 10000.00

  Scenario: Debit from account with sufficient balance
    Given an account with ID "ACC-001" exists with balance 10000.00
    When I debit 1000.00 from account "ACC-001"
    Then the new balance should be 9000.00
```

---

## 7. CI/CD Pipeline 設計

### Decision
採用獨立 Pipeline 策略，Provider 和 Consumer 各自有獨立的 CI 流程，加上定期契約驗證工作流程。

### Rationale
- 獨立 Pipeline 確保部署獨立性
- Provider 變更時自動發布 Stub
- Consumer 變更時自動驗證契約相容性
- 定期驗證確保長期契約一致性

### Pipeline Flow

**Provider Pipeline**:
```
Code Push → Build → Contract Test → Generate Stub → Publish Stub → Docker Build
```

**Consumer Pipeline**:
```
Code Push → Build → Unit Test → Download Stub → Contract Test → Integration Test → Docker Build
```

**Contract Verification (Scheduled)**:
```
Schedule → Checkout Provider → For each Consumer → Verify Contracts → Notify on Failure
```

### Gradle Commands

| 指令 | 說明 |
|------|------|
| `./gradlew build` | 完整建置含測試 |
| `./gradlew contractTest` | 只執行契約測試 |
| `./gradlew generateContractTests` | 產生契約測試類別 |
| `./gradlew publishToMavenLocal` | 發布 Stub 到本地 |
| `./gradlew verifierStubsJar` | 產生 Stub JAR |

---

## 8. 可觀測性實作

### Decision
採用完整可觀測性支援，包含結構化日誌、執行指標和分散式追蹤。

### Rationale
- 結構化日誌便於問題診斷和搜尋
- 指標監控 CI Pipeline 執行時間和成功率
- 分散式追蹤便於追蹤跨服務請求

### Implementation Approach

| 面向 | 技術選擇 | 用途 |
|------|---------|------|
| 日誌 | Logback + JSON Encoder | 結構化日誌，包含 request/response 細節 |
| 指標 | Micrometer + Prometheus | CI 執行時間、測試通過率、Stub 下載時間 |
| 追蹤 | Spring Cloud Sleuth | 跨服務請求追蹤，便於除錯 |

---

## 9. 敏感資料處理

### Decision
契約中所有敏感資料使用遮罩/虛擬資料，格式如 `ACC-XXX`、`***`。

### Rationale
- 避免真實資料外洩
- 契約仍能驗證資料格式
- 符合資安最佳實踐

### Data Masking Patterns

| 資料類型 | 遮罩格式 | 範例 |
|---------|---------|------|
| 帳戶 ID | ACC-XXX | ACC-001, ACC-999 |
| 持有人姓名 | 中文字元遮罩 | 王大明 (測試用) |
| 餘額 | 測試金額 | 10000.00 |
| 交易 ID | TXN-YYYYMMDD-XXX | TXN-20240115-001 |

---

## 10. 錯誤處理與重試機制

### Decision
儲存庫連線失敗時執行重試機制（最多 3 次），若仍失敗則使用本地快取並發出警告。

### Rationale
- 重試機制處理暫時性網路問題
- 本地快取確保 CI 不因暫時性問題中斷
- 警告通知潛在的版本不一致風險

### Retry Configuration
```yaml
stubrunner:
  repositoryRoot: ${NEXUS_URL}
  retry:
    maxAttempts: 3
    backoffPeriod: 1000
  fallbackToLocalCache: true
  warnOnCacheFallback: true
```

---

## Summary

所有研究項目已完成，無 NEEDS CLARIFICATION 項目。技術決策符合 Constitution 原則：

| 決策項目 | Constitution 原則對應 |
|---------|---------------------|
| Spring Cloud Contract | III. 契約驅動開發 |
| 六角形架構 | VI. 六角形架構 |
| Design by Contract 契約結構 | III. 契約驅動開發 |
| TDD/BDD 測試流程 | II. 測試優先 |
| Cucumber BDD | II. 測試優先 (BDD) |
| Gradle 建置 | Tech.md 規範 |
| 可觀測性 | 開發工作流程 (完整可觀測性) |
