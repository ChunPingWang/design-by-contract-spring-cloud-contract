# Spring Cloud Contract + Gitea Actions 技術規格文件

> **版本**: 1.0.0  
> **適用對象**: AI 輔助開發、DevOps 工程師、後端開發人員  
> **技術棧**: Java 17+, Spring Boot 3.x, Spring Cloud Contract 4.x, Gitea Actions

---

## 1. 架構概覽

### 1.1 整體架構圖

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Contract Testing Architecture                         │
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                      Gitea Repository                            │    │
│  │  ┌─────────────────┐              ┌─────────────────┐           │    │
│  │  │ account-service │              │ payment-service │           │    │
│  │  │   (Provider)    │              │   (Consumer)    │           │    │
│  │  └────────┬────────┘              └────────┬────────┘           │    │
│  └───────────┼────────────────────────────────┼─────────────────────┘    │
│              │                                │                          │
│              ▼                                ▼                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                     Gitea Actions CI/CD                          │    │
│  │                                                                  │    │
│  │  Provider Pipeline:                Consumer Pipeline:            │    │
│  │  1. Build & Test                   1. Build & Test               │    │
│  │  2. Generate Stubs                 2. Download Stubs             │    │
│  │  3. Publish to Nexus/Registry      3. Run Contract Tests         │    │
│  │  4. Verify against Pact Broker     4. Integration Tests          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│              │                                │                          │
│              ▼                                ▼                          │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │                    Artifact Repository                           │    │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │    │
│  │  │   Nexus     │    │  Harbor     │    │ Pact Broker │          │    │
│  │  │ (Stub JARs) │    │  (Images)   │    │ (Contracts) │          │    │
│  │  └─────────────┘    └─────────────┘    └─────────────┘          │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

### 1.2 Contract Testing 流程

```
┌──────────────────────────────────────────────────────────────────────┐
│                 Spring Cloud Contract 工作流程                        │
│                                                                       │
│  Phase 1: Contract Definition (Provider 端)                          │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │  contracts/                                                  │     │
│  │  └── account/                                               │     │
│  │      ├── getAccount.groovy      ← 定義 GET /accounts/{id}   │     │
│  │      ├── createAccount.groovy   ← 定義 POST /accounts       │     │
│  │      └── debitAccount.groovy    ← 定義 POST /accounts/debit │     │
│  └─────────────────────────────────────────────────────────────┘     │
│                              │                                        │
│                              ▼                                        │
│  Phase 2: Auto-Generate (Maven Plugin)                               │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │  mvn spring-cloud-contract:generateTests                    │     │
│  │                              │                               │     │
│  │              ┌───────────────┴───────────────┐               │     │
│  │              ▼                               ▼               │     │
│  │  ┌─────────────────────┐      ┌─────────────────────┐       │     │
│  │  │ Generated Tests     │      │ Stub JAR            │       │     │
│  │  │ (驗證 Provider)     │      │ (給 Consumer 使用)   │       │     │
│  │  └─────────────────────┘      └─────────────────────┘       │     │
│  └─────────────────────────────────────────────────────────────┘     │
│                                                                       │
│  Phase 3: Consumer Integration                                       │
│  ┌─────────────────────────────────────────────────────────────┐     │
│  │  @AutoConfigureStubRunner(                                  │     │
│  │      ids = "com.example:account-service:+:stubs:8090"       │     │
│  │  )                                                          │     │
│  │  class PaymentServiceContractTest { ... }                   │     │
│  └─────────────────────────────────────────────────────────────┘     │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 2. 專案結構

### 2.1 Provider 專案結構 (account-service)

```
account-service/
├── .gitea/
│   └── workflows/
│       ├── ci.yaml                    # 主要 CI pipeline
│       ├── contract-publish.yaml      # 發布 Stub JAR
│       └── contract-verify.yaml       # 驗證契約
├── src/
│   ├── main/
│   │   ├── java/com/example/account/
│   │   │   ├── AccountServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── AccountController.java
│   │   │   ├── service/
│   │   │   │   ├── AccountService.java
│   │   │   │   └── AccountServiceImpl.java
│   │   │   ├── domain/
│   │   │   │   ├── Account.java
│   │   │   │   ├── DebitRequest.java
│   │   │   │   └── DebitResponse.java
│   │   │   └── repository/
│   │   │       └── AccountRepository.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/example/account/
│       │   ├── ContractVerifierBase.java   # 契約測試基礎類別
│       │   └── controller/
│       │       └── AccountControllerTest.java
│       └── resources/
│           └── contracts/                   # 契約定義目錄
│               └── account/
│                   ├── getAccount.groovy
│                   ├── getAccountNotFound.groovy
│                   ├── createAccount.groovy
│                   ├── debitAccount.groovy
│                   └── debitInsufficientBalance.groovy
├── pom.xml
├── Dockerfile
└── README.md
```

### 2.2 Consumer 專案結構 (payment-service)

```
payment-service/
├── .gitea/
│   └── workflows/
│       ├── ci.yaml                    # 主要 CI pipeline
│       └── contract-test.yaml         # 契約測試
├── src/
│   ├── main/
│   │   ├── java/com/example/payment/
│   │   │   ├── PaymentServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── PaymentController.java
│   │   │   ├── service/
│   │   │   │   └── PaymentService.java
│   │   │   ├── client/
│   │   │   │   └── AccountClient.java      # Feign Client
│   │   │   └── domain/
│   │   │       └── PaymentRequest.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/example/payment/
│       │   └── contract/
│       │       └── AccountClientContractTest.java
│       └── resources/
│           └── application-contract-test.yml
├── pom.xml
├── Dockerfile
└── README.md
```

---

## 3. Provider 端實作

### 3.1 Maven 配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>account-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <spring-cloud-contract.version>4.1.0</spring-cloud-contract.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- Database -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Spring Cloud Contract Verifier -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-contract-verifier</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
            
            <!-- Spring Cloud Contract Plugin -->
            <plugin>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-contract-maven-plugin</artifactId>
                <version>${spring-cloud-contract.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <!-- 契約測試基礎類別 -->
                    <baseClassForTests>
                        com.example.account.ContractVerifierBase
                    </baseClassForTests>
                    <!-- 契約目錄 -->
                    <contractsDirectory>
                        ${project.basedir}/src/test/resources/contracts
                    </contractsDirectory>
                    <!-- 產生的測試目錄 -->
                    <generatedTestSourcesDir>
                        ${project.build.directory}/generated-test-sources/contracts
                    </generatedTestSourcesDir>
                    <!-- 產生的 Stub 目錄 -->
                    <stubsOutputDirectory>
                        ${project.build.directory}/stubs
                    </stubsOutputDirectory>
                </configuration>
            </plugin>
        </plugins>
    </build>
    
    <!-- 發布 Stub JAR 到 Nexus -->
    <distributionManagement>
        <repository>
            <id>nexus-releases</id>
            <url>${env.NEXUS_URL}/repository/maven-releases/</url>
        </repository>
        <snapshotRepository>
            <id>nexus-snapshots</id>
            <url>${env.NEXUS_URL}/repository/maven-snapshots/</url>
        </snapshotRepository>
    </distributionManagement>
</project>
```

### 3.2 契約定義檔案

#### 3.2.1 查詢帳戶契約 (getAccount.groovy)

```groovy
// src/test/resources/contracts/account/getAccount.groovy
package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "查詢帳戶 - 成功"
    description """
        當帳戶存在時，返回帳戶詳細資訊
        
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
            accountHolder: $(consumer("王大明"), producer(regex("[\\u4e00-\\u9fa5]{2,10}"))),
            balance: $(consumer(10000.00), producer(anyDouble())),
            currency: "TWD",
            status: $(consumer("ACTIVE"), producer(regex("ACTIVE|FROZEN|CLOSED"))),
            createdAt: $(consumer("2024-01-15T10:30:00Z"), producer(regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"))),
            updatedAt: $(consumer("2024-01-15T10:30:00Z"), producer(regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")))
        ])
        bodyMatchers {
            jsonPath('$.balance', byType {
                minOccurrence(1)
            })
        }
    }
}
```

#### 3.2.2 帳戶不存在契約 (getAccountNotFound.groovy)

```groovy
// src/test/resources/contracts/account/getAccountNotFound.groovy
package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "查詢帳戶 - 不存在"
    description """
        當帳戶不存在時，返回 404 錯誤
        
        Contract:
        - Precondition: accountId 格式正確但帳戶不存在
        - Postcondition: 返回 404 及錯誤訊息
    """
    
    request {
        method GET()
        url "/api/v1/accounts/ACC-999"
        headers {
            accept(applicationJson())
        }
    }
    
    response {
        status NOT_FOUND()
        headers {
            contentType(applicationJson())
        }
        body([
            errorCode: "ACCOUNT_NOT_FOUND",
            message: "帳戶不存在",
            timestamp: $(producer(regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z"))),
            path: "/api/v1/accounts/ACC-999"
        ])
    }
}
```

#### 3.2.3 扣款契約 (debitAccount.groovy)

```groovy
// src/test/resources/contracts/account/debitAccount.groovy
package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "帳戶扣款 - 成功"
    description """
        當餘額充足時，執行扣款並返回新餘額
        
        Contract:
        - Precondition: amount > 0, amount <= balance, 帳戶狀態為 ACTIVE
        - Postcondition: newBalance = oldBalance - amount
        - Invariant: balance >= 0
    """
    
    request {
        method POST()
        url "/api/v1/accounts/ACC-001/debit"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body([
            transactionId: $(consumer("TXN-20240115-001"), producer(regex("TXN-[0-9]{8}-[0-9]{3}"))),
            amount: $(consumer(1000.00), producer(anyDouble())),
            currency: "TWD",
            description: $(consumer(optional("消費扣款")), producer(anyNonEmptyString())),
            merchantId: $(consumer(optional("MERCHANT-001")), producer(optional(anyNonEmptyString())))
        ])
    }
    
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            transactionId: fromRequest().body('$.transactionId'),
            accountId: "ACC-001",
            status: "SUCCESS",
            previousBalance: $(producer(anyDouble())),
            amount: fromRequest().body('$.amount'),
            newBalance: $(producer(anyDouble())),
            processedAt: $(producer(regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")))
        ])
    }
}
```

#### 3.2.4 餘額不足契約 (debitInsufficientBalance.groovy)

```groovy
// src/test/resources/contracts/account/debitInsufficientBalance.groovy
package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "帳戶扣款 - 餘額不足"
    description """
        當餘額不足時，拒絕扣款
        
        Contract:
        - Precondition: amount > balance
        - Postcondition: 返回 400 錯誤，餘額不變
    """
    
    request {
        method POST()
        url "/api/v1/accounts/ACC-002/debit"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body([
            transactionId: "TXN-20240115-002",
            amount: 99999.00,
            currency: "TWD"
        ])
    }
    
    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body([
            errorCode: "INSUFFICIENT_BALANCE",
            message: "餘額不足",
            details: [
                accountId: "ACC-002",
                currentBalance: $(producer(anyDouble())),
                requestedAmount: 99999.00,
                shortfall: $(producer(anyDouble()))
            ],
            timestamp: $(producer(regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}Z")))
        ])
    }
}
```

### 3.3 契約測試基礎類別

```java
// src/test/java/com/example/account/ContractVerifierBase.java
package com.example.account;

import com.example.account.controller.AccountController;
import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.DebitRequest;
import com.example.account.domain.DebitResponse;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.exception.InsufficientBalanceException;
import com.example.account.service.AccountService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

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
        
        setupGetAccountSuccess();
        setupGetAccountNotFound();
        setupDebitSuccess();
        setupDebitInsufficientBalance();
    }

    /**
     * 設置查詢帳戶成功的 Mock
     * Provider State: 帳戶 ACC-001 存在
     */
    private void setupGetAccountSuccess() {
        Account account = Account.builder()
            .accountId("ACC-001")
            .accountHolder("王大明")
            .balance(new BigDecimal("10000.00"))
            .currency("TWD")
            .status(AccountStatus.ACTIVE)
            .createdAt(Instant.parse("2024-01-15T10:30:00Z"))
            .updatedAt(Instant.parse("2024-01-15T10:30:00Z"))
            .build();

        given(accountService.getAccount("ACC-001"))
            .willReturn(account);
    }

    /**
     * 設置查詢帳戶不存在的 Mock
     * Provider State: 帳戶 ACC-999 不存在
     */
    private void setupGetAccountNotFound() {
        given(accountService.getAccount("ACC-999"))
            .willThrow(new AccountNotFoundException("ACC-999"));
    }

    /**
     * 設置扣款成功的 Mock
     * Provider State: 帳戶 ACC-001 餘額充足
     */
    private void setupDebitSuccess() {
        given(accountService.debit(eq("ACC-001"), any(DebitRequest.class)))
            .willAnswer(invocation -> {
                DebitRequest request = invocation.getArgument(1);
                return DebitResponse.builder()
                    .transactionId(request.getTransactionId())
                    .accountId("ACC-001")
                    .status("SUCCESS")
                    .previousBalance(new BigDecimal("10000.00"))
                    .amount(request.getAmount())
                    .newBalance(new BigDecimal("10000.00").subtract(request.getAmount()))
                    .processedAt(Instant.now())
                    .build();
            });
    }

    /**
     * 設置餘額不足的 Mock
     * Provider State: 帳戶 ACC-002 餘額不足
     */
    private void setupDebitInsufficientBalance() {
        given(accountService.debit(eq("ACC-002"), any(DebitRequest.class)))
            .willThrow(new InsufficientBalanceException(
                "ACC-002",
                new BigDecimal("500.00"),
                new BigDecimal("99999.00")
            ));
    }
}
```

### 3.4 Domain 類別

```java
// src/main/java/com/example/account/domain/Account.java
package com.example.account.domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    @Id
    private String accountId;
    
    private String accountHolder;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal balance;
    
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    
    private Instant createdAt;
    
    private Instant updatedAt;
}
```

```java
// src/main/java/com/example/account/domain/DebitRequest.java
package com.example.account.domain;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitRequest {
    
    @NotBlank(message = "transactionId 不可為空")
    @Pattern(regexp = "TXN-[0-9]{8}-[0-9]{3}", message = "transactionId 格式不正確")
    private String transactionId;
    
    @NotNull(message = "amount 不可為空")
    @DecimalMin(value = "0.01", message = "amount 必須大於 0")
    private BigDecimal amount;
    
    @NotBlank(message = "currency 不可為空")
    private String currency;
    
    private String description;
    
    private String merchantId;
}
```

```java
// src/main/java/com/example/account/domain/DebitResponse.java
package com.example.account.domain;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitResponse {
    private String transactionId;
    private String accountId;
    private String status;
    private BigDecimal previousBalance;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private Instant processedAt;
}
```

### 3.5 Controller

```java
// src/main/java/com/example/account/controller/AccountController.java
package com.example.account.controller;

import com.example.account.domain.*;
import com.example.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountId}/debit")
    public ResponseEntity<DebitResponse> debit(
            @PathVariable String accountId,
            @Valid @RequestBody DebitRequest request) {
        DebitResponse response = accountService.debit(accountId, request);
        return ResponseEntity.ok(response);
    }
}
```

---

## 4. Consumer 端實作

### 4.1 Maven 配置 (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
        <relativePath/>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>payment-service</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- OpenFeign for HTTP Client -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        
        <!-- Spring Cloud Contract Stub Runner -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-contract-stub-runner</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

### 4.2 Feign Client

```java
// src/main/java/com/example/payment/client/AccountClient.java
package com.example.payment.client;

import com.example.payment.client.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "account-service",
    url = "${account-service.url}",
    fallback = AccountClientFallback.class
)
public interface AccountClient {

    @GetMapping("/api/v1/accounts/{accountId}")
    AccountResponse getAccount(@PathVariable("accountId") String accountId);

    @PostMapping("/api/v1/accounts/{accountId}/debit")
    DebitResponse debit(
        @PathVariable("accountId") String accountId,
        @RequestBody DebitRequest request
    );
}
```

### 4.3 Consumer 契約測試

```java
// src/test/java/com/example/payment/contract/AccountClientContractTest.java
package com.example.payment.contract;

import com.example.payment.client.AccountClient;
import com.example.payment.client.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(
    ids = "com.example:account-service:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
    // 或使用遠端 Repository
    // stubsMode = StubRunnerProperties.StubsMode.REMOTE,
    // repositoryRoot = "https://nexus.example.com/repository/maven-snapshots/"
)
@ActiveProfiles("contract-test")
class AccountClientContractTest {

    @Autowired
    private AccountClient accountClient;

    @Test
    void shouldGetAccountSuccessfully() {
        // Act
        AccountResponse response = accountClient.getAccount("ACC-001");

        // Assert - 驗證契約中定義的 Postcondition
        assertThat(response.getAccountId()).isEqualTo("ACC-001");
        assertThat(response.getBalance()).isNotNull();
        assertThat(response.getStatus()).isIn("ACTIVE", "FROZEN", "CLOSED");
    }

    @Test
    void shouldDebitAccountSuccessfully() {
        // Arrange
        DebitRequest request = DebitRequest.builder()
            .transactionId("TXN-20240115-001")
            .amount(new BigDecimal("1000.00"))
            .currency("TWD")
            .description("消費扣款")
            .build();

        // Act
        DebitResponse response = accountClient.debit("ACC-001", request);

        // Assert
        assertThat(response.getTransactionId()).isEqualTo("TXN-20240115-001");
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getNewBalance()).isNotNull();
    }

    @Test
    void shouldHandleInsufficientBalance() {
        // Arrange
        DebitRequest request = DebitRequest.builder()
            .transactionId("TXN-20240115-002")
            .amount(new BigDecimal("99999.00"))
            .currency("TWD")
            .build();

        // Act & Assert
        assertThatThrownBy(() -> accountClient.debit("ACC-002", request))
            .isInstanceOf(FeignException.BadRequest.class);
    }
}
```

### 4.4 測試配置

```yaml
# src/test/resources/application-contract-test.yml
account-service:
  url: http://localhost:8090

spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 5000
            readTimeout: 5000

logging:
  level:
    org.springframework.cloud.contract: DEBUG
    com.example.payment.client: DEBUG
```

---

## 5. Gitea Actions CI/CD

### 5.1 Provider CI Pipeline

```yaml
# .gitea/workflows/ci.yaml
name: Account Service CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  JAVA_VERSION: '17'
  MAVEN_OPTS: '-Xmx1024m'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build and Test
        run: |
          mvn clean verify -B \
            -Dmaven.test.failure.ignore=false

      - name: Generate Contract Tests
        run: |
          mvn spring-cloud-contract:generateTests -B

      - name: Run Contract Tests
        run: |
          mvn test -Dtest=*ContractTest -B

      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/

      - name: Upload Stubs
        uses: actions/upload-artifact@v4
        with:
          name: stubs
          path: target/stubs/

  publish-stubs:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven
          server-id: nexus-snapshots
          server-username: NEXUS_USERNAME
          server-password: NEXUS_PASSWORD

      - name: Publish Stubs to Nexus
        env:
          NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
          NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
          NEXUS_URL: ${{ secrets.NEXUS_URL }}
        run: |
          mvn deploy -B \
            -DskipTests \
            -Dspring-cloud-contract.skip=false

  docker:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build JAR
        run: mvn package -DskipTests -B

      - name: Login to Harbor
        uses: docker/login-action@v3
        with:
          registry: ${{ secrets.HARBOR_REGISTRY }}
          username: ${{ secrets.HARBOR_USERNAME }}
          password: ${{ secrets.HARBOR_PASSWORD }}

      - name: Build and Push Docker Image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.HARBOR_REGISTRY }}/microservices/account-service:${{ github.sha }}
            ${{ secrets.HARBOR_REGISTRY }}/microservices/account-service:latest
```

### 5.2 Consumer CI Pipeline

```yaml
# .gitea/workflows/ci.yaml
name: Payment Service CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  JAVA_VERSION: '17'

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build
        run: mvn clean compile -B

      - name: Unit Tests
        run: mvn test -B -Dtest=!*ContractTest

  contract-test:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven
          server-id: nexus-snapshots
          server-username: NEXUS_USERNAME
          server-password: NEXUS_PASSWORD

      - name: Download Provider Stubs and Run Contract Tests
        env:
          NEXUS_USERNAME: ${{ secrets.NEXUS_USERNAME }}
          NEXUS_PASSWORD: ${{ secrets.NEXUS_PASSWORD }}
        run: |
          mvn test -B \
            -Dtest=*ContractTest \
            -Dstubrunner.repository-root=${{ secrets.NEXUS_URL }}/repository/maven-snapshots/ \
            -Dstubrunner.stubs-mode=REMOTE

      - name: Upload Contract Test Results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: contract-test-results
          path: target/surefire-reports/

  integration-test:
    needs: contract-test
    runs-on: ubuntu-latest
    services:
      account-service-stub:
        image: ${{ secrets.HARBOR_REGISTRY }}/tools/wiremock:latest
        ports:
          - 8090:8080
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Integration Tests
        run: |
          mvn verify -B \
            -DskipUnitTests \
            -Daccount-service.url=http://localhost:8090

  docker:
    needs: [contract-test, integration-test]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Build JAR
        run: mvn package -DskipTests -B

      - name: Login to Harbor
        uses: docker/login-action@v3
        with:
          registry: ${{ secrets.HARBOR_REGISTRY }}
          username: ${{ secrets.HARBOR_USERNAME }}
          password: ${{ secrets.HARBOR_PASSWORD }}

      - name: Build and Push
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: |
            ${{ secrets.HARBOR_REGISTRY }}/microservices/payment-service:${{ github.sha }}
            ${{ secrets.HARBOR_REGISTRY }}/microservices/payment-service:latest
```

### 5.3 Contract Verification Workflow (定期驗證)

```yaml
# .gitea/workflows/contract-verify.yaml
name: Contract Verification

on:
  schedule:
    # 每天 UTC 02:00 執行
    - cron: '0 2 * * *'
  workflow_dispatch:

jobs:
  verify-contracts:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        consumer:
          - payment-service
          - notification-service
          - audit-service
    steps:
      - name: Checkout Account Service
        uses: actions/checkout@v4
        with:
          repository: your-org/account-service

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Verify Contracts from ${{ matrix.consumer }}
        env:
          PACT_BROKER_URL: ${{ secrets.PACT_BROKER_URL }}
          PACT_BROKER_TOKEN: ${{ secrets.PACT_BROKER_TOKEN }}
        run: |
          mvn test -B \
            -Dtest=ContractVerifierBase \
            -Dpact.verifier.publishResults=true \
            -Dpact.provider.version=${{ github.sha }} \
            -Dpactbroker.host=${PACT_BROKER_URL}

      - name: Notify on Failure
        if: failure()
        uses: actions/github-script@v7
        with:
          script: |
            const message = `Contract verification failed for consumer: ${{ matrix.consumer }}`;
            // Send notification to Slack/Teams/Email
```

---

## 6. AI 輔助開發指引

### 6.1 Prompt 範本：新增契約

```markdown
## 任務：為 Account Service 新增契約

### 背景
- Provider: account-service
- Consumer: payment-service
- 技術棧: Spring Cloud Contract 4.x

### 需求
新增一個 API 契約用於「帳戶凍結」功能

### API 規格
- Endpoint: POST /api/v1/accounts/{accountId}/freeze
- Request Body: { "reason": "string", "operatorId": "string" }
- Success Response: 200 OK with account details
- Error Response: 404 if account not found, 400 if already frozen

### 請產出
1. Groovy 契約檔案 (freezeAccount.groovy)
2. 更新 ContractVerifierBase.java 的 Mock 設置
3. Consumer 端測試案例
```

### 6.2 Prompt 範本：除錯契約測試

```markdown
## 任務：除錯 Contract Test 失敗

### 錯誤訊息
```
ContractVerificationException: 
Expected status code <200> but was <500>
Response body: {"error":"NullPointerException"}
```

### 契約檔案
[貼上相關 .groovy 檔案]

### ContractVerifierBase
[貼上 Base 類別]

### 請分析
1. 可能的根因
2. 修復建議
3. 預防措施
```

### 6.3 程式碼產生 Checklist

```yaml
contract_development_checklist:
  provider_side:
    - [ ] 契約檔案命名符合規範 (動詞+名詞.groovy)
    - [ ] 包含完整的 request/response 定義
    - [ ] 使用適當的 matcher (regex, anyDouble, etc.)
    - [ ] description 包含 DbC 條件說明
    - [ ] ContractVerifierBase 有對應的 Mock 設置
    
  consumer_side:
    - [ ] Stub Runner 配置正確
    - [ ] 測試涵蓋成功和失敗場景
    - [ ] 適當的 assertion 驗證回應
    
  ci_cd:
    - [ ] Provider 發布 Stub JAR
    - [ ] Consumer 可下載並測試
    - [ ] 失敗時有通知機制
```

---

## 7. 附錄

### 7.1 常用 Contract DSL 語法

| 語法 | 說明 | 範例 |
|------|------|------|
| `$(consumer(...), producer(...))` | 分別定義 Consumer/Provider 值 | `$(consumer("test"), producer(regex(".*")))` |
| `anyDouble()` | 任意浮點數 | `balance: $(producer(anyDouble()))` |
| `regex(...)` | 正則表達式匹配 | `$(producer(regex("[A-Z]{3}")))` |
| `optional(...)` | 可選欄位 | `description: $(consumer(optional("desc")))` |
| `fromRequest()` | 引用 Request 值 | `transactionId: fromRequest().body('$.transactionId')` |

### 7.2 故障排除

| 問題 | 可能原因 | 解決方案 |
|------|---------|---------|
| Stub not found | Stub JAR 未發布 | 確認 Provider CI 已執行 publish |
| Contract test timeout | Stub Runner 啟動慢 | 增加 timeout 或使用本地 Stub |
| Response mismatch | Mock 設置不匹配 | 檢查 ContractVerifierBase |
