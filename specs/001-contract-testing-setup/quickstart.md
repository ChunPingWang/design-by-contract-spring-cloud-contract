# Quickstart: Spring Cloud Contract 契約測試系統

**Feature**: 001-contract-testing-setup
**Date**: 2025-12-16

## 概述

本指南幫助開發人員快速上手 Spring Cloud Contract 契約測試系統，包含 Provider (account-service) 和 Consumer (payment-service) 的開發流程。

## 先決條件

- Java 17+
- Gradle 8.x+
- Docker (可選，用於容器化部署)
- Git

## 快速開始

### 1. 環境設置

```bash
# 克隆專案
git clone <repository-url>
cd design-by-contract-spring-cloud-contract

# 切換到功能分支
git checkout 001-contract-testing-setup
```

### 2. 完整建置

```bash
# 建置所有模組並執行測試
./gradlew clean build

# 預期結果: BUILD SUCCESSFUL
```

### 3. Provider 開發流程 (account-service)

#### 3.1 契約位置

契約檔案位於 `account-service/src/test/resources/contracts/account/`:

| 契約檔案 | 說明 |
|---------|------|
| `getAccount.groovy` | 查詢帳戶成功 |
| `getAccountNotFound.groovy` | 帳戶不存在 |
| `createAccount.groovy` | 建立帳戶 |
| `debitAccount.groovy` | 扣款成功 |
| `debitInsufficientBalance.groovy` | 餘額不足 |
| `freezeAccount.groovy` | 凍結帳戶 |
| `unfreezeAccount.groovy` | 解凍帳戶 |
| `getAccountWithCreatedAt.groovy` | 向後相容測試 (新增 createdAt 欄位) |

#### 3.2 契約範例

```groovy
// getAccount.groovy
Contract.make {
    name "get_account_success"
    description """
        Contract: 查詢帳戶成功
        Version: 1.0.0

        Precondition: accountId 存在於系統中
        Postcondition: 返回帳戶資訊
        Invariant: balance >= 0
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-001"
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-001",
            ownerName: "王大明",
            balance: 10000.00,
            status: "ACTIVE"
        ])
    }
}
```

#### 3.3 Base 測試類別

```java
// account-service/src/test/java/com/example/account/ContractVerifierBase.java
@WebMvcTest(controllers = {AccountController.class, GlobalExceptionHandler.class})
public abstract class ContractVerifierBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        // 設置各契約的 Mock 資料
        setupGetAccountContract();
        // ...
    }
}
```

#### 3.4 執行 Provider 測試

```bash
# 執行契約測試
./gradlew :account-service:contractTest

# 完整建置並產生 Stub
./gradlew :account-service:build

# 發布 Stub 到本地 Maven 倉庫
./gradlew :account-service:publishToMavenLocal
```

### 4. Consumer 開發流程 (payment-service)

#### 4.1 Stub Runner 配置

```java
// payment-service/src/test/java/com/example/payment/contract/AccountClientContractTest.java
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
        assertThat(account.balance()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }
}
```

#### 4.2 執行 Consumer 測試

```bash
# 先確保 Stub 已發布
./gradlew :account-service:publishToMavenLocal

# 執行 Consumer 測試
./gradlew :payment-service:test
```

### 5. Cucumber BDD 測試

#### 5.1 Feature 檔案

**account-service** (`account-service/src/test/resources/features/account.feature`):

```gherkin
Feature: 帳戶管理功能
  作為帳戶持有人
  我希望能夠管理我的帳戶
  以便追蹤我的餘額和交易

  Scenario: 查詢現有帳戶
    Given 系統中存在帳號 "ACC-001" 餘額為 10000.00
    When 我查詢帳號 "ACC-001"
    Then 我應該看到餘額為 10000.00
```

**payment-service** (`payment-service/src/test/resources/features/payment.feature`):

```gherkin
Feature: 支付功能
  作為支付服務
  我需要與帳戶服務整合
  以便處理支付交易

  Scenario: 成功支付
    Given 帳戶 "ACC-001" 存在且餘額充足
    When 我發起支付 1000.00 從帳戶 "ACC-001"
    Then 支付應該成功
    And 新餘額應該是 9000.00
```

#### 5.2 執行 Cucumber 測試

```bash
# 執行所有 Cucumber 測試
./gradlew test --tests "*Cucumber*"

# 執行特定模組的 Cucumber 測試
./gradlew :account-service:test --tests "*Cucumber*"
./gradlew :payment-service:test --tests "*Cucumber*"
```

### 6. TDD 開發週期

```
1. 定義契約 (Red)
   └─► 在 contracts/ 目錄新增 Groovy 契約

2. 執行測試 (Red)
   └─► ./gradlew :account-service:contractTest 失敗

3. 更新 Base 類別
   └─► 在 ContractVerifierBase 新增 Mock 設定

4. 實作功能 (Green)
   └─► 實作 Controller/Service

5. 測試通過 (Green)
   └─► ./gradlew :account-service:build 成功

6. 發布 Stub
   └─► ./gradlew :account-service:publishToMavenLocal
```

## 常見指令

| 指令 | 說明 |
|------|------|
| `./gradlew clean build` | 完整建置所有模組含測試 |
| `./gradlew :account-service:build` | 建置 Provider 模組 |
| `./gradlew :payment-service:build` | 建置 Consumer 模組 |
| `./gradlew :account-service:contractTest` | 只執行 Provider 契約測試 |
| `./gradlew :account-service:generateContractTests` | 產生契約測試類別 |
| `./gradlew :account-service:publishToMavenLocal` | 發布 Stub 到本地 Maven |
| `./gradlew :account-service:verifierStubsJar` | 產生 Stub JAR |
| `./gradlew test --tests "*Cucumber*"` | 執行所有 Cucumber 測試 |
| `./gradlew clean` | 清除建置產物 |

## 目錄結構

```
design-by-contract-spring-cloud-contract/
├── account-service/
│   ├── src/
│   │   ├── main/java/com/example/account/
│   │   │   ├── AccountServiceApplication.java
│   │   │   ├── domain/
│   │   │   │   ├── Account.java
│   │   │   │   ├── AccountStatus.java
│   │   │   │   ├── DebitRequest.java
│   │   │   │   └── DebitResponse.java
│   │   │   ├── application/
│   │   │   │   ├── AccountService.java
│   │   │   │   └── AccountServiceImpl.java
│   │   │   └── infrastructure/
│   │   │       ├── controller/AccountController.java
│   │   │       ├── repository/AccountRepository.java
│   │   │       ├── dto/
│   │   │       └── exception/
│   │   └── test/
│   │       ├── java/com/example/account/
│   │       │   ├── ContractVerifierBase.java
│   │       │   └── cucumber/
│   │       └── resources/
│   │           ├── contracts/account/*.groovy
│   │           └── features/account.feature
│   ├── build.gradle
│   └── Dockerfile
├── payment-service/
│   ├── src/
│   │   ├── main/java/com/example/payment/
│   │   │   ├── PaymentServiceApplication.java
│   │   │   ├── domain/PaymentRequest.java
│   │   │   ├── application/PaymentService.java
│   │   │   └── infrastructure/
│   │   │       ├── controller/PaymentController.java
│   │   │       └── client/
│   │   │           ├── AccountClient.java
│   │   │           └── dto/
│   │   └── test/
│   │       ├── java/com/example/payment/
│   │       │   ├── contract/AccountClientContractTest.java
│   │       │   └── cucumber/
│   │       └── resources/
│   │           └── features/payment.feature
│   ├── build.gradle
│   └── Dockerfile
├── docs/
│   ├── contract-versioning.md
│   ├── ci-cd-setup.md
│   └── breaking-change-detection.md
├── .github/workflows/  (或 .gitea/workflows/)
│   ├── account-service-ci.yaml
│   ├── payment-service-ci.yaml
│   └── contract-verify.yaml
├── build.gradle
└── settings.gradle
```

## Design by Contract 原則

每個契約必須包含：

| 元素 | 說明 | 範例 |
|------|------|------|
| Precondition | 呼叫前必須滿足的條件 | accountNumber 格式正確、帳戶存在 |
| Postcondition | 呼叫後保證的結果 | 返回完整帳戶資訊 |
| Invariant | 始終保持的約束 | balance >= 0 |

## 契約版本管理

### 安全變更 (不需更新 Consumer)

- 新增可選欄位到回應 (如 `createdAt`)
- 新增新的 API 端點
- 將必填欄位改為可選

### 破壞性變更 (需要更新 Consumer)

- 移除回應欄位
- 變更欄位類型
- 重新命名欄位
- 變更 URL 路徑
- 將可選欄位改為必填

詳見 `docs/breaking-change-detection.md`

## 故障排除

### 契約測試失敗

1. 確認 `ContractVerifierBase` 正確設置 Mock
2. 檢查契約檔案 Groovy 語法
3. 確認 response body 與實際回傳一致

### Stub Runner 找不到 Stub

1. 確認已執行 `./gradlew :account-service:publishToMavenLocal`
2. 檢查 `~/.m2/repository/com/example/account-service/` 是否有 stubs JAR
3. 確認 `ids` 格式正確: `groupId:artifactId:version:stubs:port`

### Consumer 測試連線失敗

1. 確認 Stub Runner 埠號與 Feign 配置一致
2. 檢查 `stubsMode` 設定為 `LOCAL`
3. 確認 application.yml 中 `account-service.url` 配置正確

### Gradle 建置問題

```bash
# 確認 Java 版本
java -version  # 需要 17+

# 確認 Gradle 版本
./gradlew --version

# 清除快取重建
./gradlew clean build --refresh-dependencies
```

## Docker 部署

```bash
# 建置 Docker 映像
docker build -t account-service:latest ./account-service
docker build -t payment-service:latest ./payment-service

# 執行容器
docker run -p 8080:8080 account-service:latest
docker run -p 8081:8081 -e ACCOUNT_SERVICE_URL=http://host.docker.internal:8080 payment-service:latest
```

## 下一步

1. 閱讀 [spec.md](./spec.md) 了解完整功能規格
2. 閱讀 [plan.md](./plan.md) 了解實作計畫
3. 閱讀 [data-model.md](./data-model.md) 了解資料模型
4. 查看 [contracts/openapi.yaml](./contracts/openapi.yaml) 了解 API 規格
5. 閱讀 [research.md](./research.md) 了解技術決策
6. 閱讀 [docs/contract-versioning.md](../../docs/contract-versioning.md) 了解契約版本管理
7. 閱讀 [docs/breaking-change-detection.md](../../docs/breaking-change-detection.md) 了解破壞性變更偵測
