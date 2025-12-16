# Tasks: Spring Cloud Contract 契約測試系統

**Input**: Design documents from `/specs/001-contract-testing-setup/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml

**Tests**: TDD/BDD approach - 契約測試先行，實作通過測試。使用 Cucumber 進行 BDD 驗收測試。

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Path Conventions

- **Multi-module Gradle project**:
  - Root: `build.gradle`, `settings.gradle`
  - Provider: `account-service/src/main/java/com/example/account/`
  - Consumer: `payment-service/src/main/java/com/example/payment/`
  - CI/CD: `.gitea/workflows/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure for both microservices using Gradle

- [ ] T001 Create root build.gradle with Spring Boot 3 and Spring Cloud Contract plugins
- [ ] T002 Create settings.gradle with multi-module configuration (account-service, payment-service)
- [ ] T003 [P] Create account-service/build.gradle with Spring Cloud Contract Verifier dependencies
- [ ] T004 [P] Create payment-service/build.gradle with Spring Cloud Contract Stub Runner dependencies
- [ ] T005 [P] Add Cucumber dependencies to account-service/build.gradle
- [ ] T006 [P] Add Cucumber dependencies to payment-service/build.gradle
- [ ] T007 [P] Create account-service/src/main/resources/application.yml with H2 and server configuration
- [ ] T008 [P] Create payment-service/src/main/resources/application.yml with Feign and server configuration
- [ ] T009 Create .gitea/workflows/ directory structure for CI/CD
- [ ] T010 Create gradlew wrapper files (gradle/wrapper/*)

**Checkpoint**: Project structure and Gradle dependencies ready

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**CRITICAL**: No user story work can begin until this phase is complete

- [ ] T011 [P] Create AccountServiceApplication.java in account-service/src/main/java/com/example/account/
- [ ] T012 [P] Create PaymentServiceApplication.java in payment-service/src/main/java/com/example/payment/
- [ ] T013 [P] Create Account domain entity in account-service/src/main/java/com/example/account/domain/Account.java
- [ ] T014 [P] Create AccountStatus enum with state machine in account-service/src/main/java/com/example/account/domain/AccountStatus.java
- [ ] T015 [P] Create DebitRequest value object in account-service/src/main/java/com/example/account/domain/DebitRequest.java
- [ ] T016 [P] Create DebitResponse value object in account-service/src/main/java/com/example/account/domain/DebitResponse.java
- [ ] T017 [P] Create ErrorResponse DTO in account-service/src/main/java/com/example/account/infrastructure/dto/ErrorResponse.java
- [ ] T018 [P] Create CreateAccountRequest DTO in account-service/src/main/java/com/example/account/infrastructure/dto/CreateAccountRequest.java
- [ ] T019 [P] Create FreezeAccountRequest DTO in account-service/src/main/java/com/example/account/infrastructure/dto/FreezeAccountRequest.java
- [ ] T020 Create AccountRepository interface in account-service/src/main/java/com/example/account/infrastructure/repository/AccountRepository.java
- [ ] T021 [P] Create AccountNotFoundException in account-service/src/main/java/com/example/account/infrastructure/exception/AccountNotFoundException.java
- [ ] T022 [P] Create InsufficientBalanceException in account-service/src/main/java/com/example/account/infrastructure/exception/InsufficientBalanceException.java
- [ ] T023 [P] Create AccountFrozenException in account-service/src/main/java/com/example/account/infrastructure/exception/AccountFrozenException.java
- [ ] T024 Create GlobalExceptionHandler in account-service/src/main/java/com/example/account/infrastructure/exception/GlobalExceptionHandler.java
- [ ] T025 Create AccountService interface in account-service/src/main/java/com/example/account/application/AccountService.java
- [ ] T026 Create AccountServiceImpl in account-service/src/main/java/com/example/account/application/AccountServiceImpl.java

**Checkpoint**: Foundation ready - Domain layer and core infrastructure complete

---

## Phase 3: User Story 1 - Provider 定義契約 (Priority: P1) MVP

**Goal**: 帳戶服務 (account-service) 定義 API 契約，自動產生驗證測試和 Stub

**Independent Test**: 執行 `./gradlew :account-service:build` 契約測試通過且產生 Stub

### Contract Tests (TDD - Write FIRST, FAIL before implementation)

- [ ] T027 [P] [US1] Create getAccount.groovy contract in account-service/src/test/resources/contracts/account/getAccount.groovy
- [ ] T028 [P] [US1] Create getAccountNotFound.groovy contract in account-service/src/test/resources/contracts/account/getAccountNotFound.groovy
- [ ] T029 [P] [US1] Create createAccount.groovy contract in account-service/src/test/resources/contracts/account/createAccount.groovy
- [ ] T030 [P] [US1] Create debitAccount.groovy contract in account-service/src/test/resources/contracts/account/debitAccount.groovy
- [ ] T031 [P] [US1] Create debitInsufficientBalance.groovy contract in account-service/src/test/resources/contracts/account/debitInsufficientBalance.groovy
- [ ] T032 [P] [US1] Create freezeAccount.groovy contract in account-service/src/test/resources/contracts/account/freezeAccount.groovy
- [ ] T033 [P] [US1] Create unfreezeAccount.groovy contract in account-service/src/test/resources/contracts/account/unfreezeAccount.groovy
- [ ] T034 [US1] Create ContractVerifierBase.java in account-service/src/test/java/com/example/account/ContractVerifierBase.java

### Cucumber BDD Tests

- [ ] T035 [P] [US1] Create account.feature in account-service/src/test/resources/features/account.feature
- [ ] T036 [US1] Create AccountSteps.java in account-service/src/test/java/com/example/account/cucumber/AccountSteps.java
- [ ] T037 [US1] Create CucumberTestRunner.java in account-service/src/test/java/com/example/account/cucumber/CucumberTestRunner.java

### Implementation for User Story 1

- [ ] T038 [US1] Implement AccountController.getAccount in account-service/src/main/java/com/example/account/infrastructure/controller/AccountController.java
- [ ] T039 [US1] Implement AccountController.createAccount in account-service/src/main/java/com/example/account/infrastructure/controller/AccountController.java
- [ ] T040 [US1] Implement AccountController.debitAccount in account-service/src/main/java/com/example/account/infrastructure/controller/AccountController.java
- [ ] T041 [US1] Implement AccountController.freezeAccount in account-service/src/main/java/com/example/account/infrastructure/controller/AccountController.java
- [ ] T042 [US1] Implement AccountController.unfreezeAccount in account-service/src/main/java/com/example/account/infrastructure/controller/AccountController.java
- [ ] T043 [US1] Run `./gradlew :account-service:build` to validate all contracts pass and Stub is generated
- [ ] T044 [US1] Run `./gradlew :account-service:publishToMavenLocal` to install Stub to local Maven repository

**Checkpoint**: User Story 1 complete - Provider contracts defined, tests pass, Stub available

---

## Phase 4: User Story 2 - Consumer 契約測試 (Priority: P2)

**Goal**: 支付服務 (payment-service) 使用 Stub Runner 執行契約測試

**Independent Test**: 執行 `./gradlew :payment-service:test` 契約測試使用 Stub 通過

### Contract Tests (TDD - Write FIRST, FAIL before implementation)

- [ ] T045 [P] [US2] Create AccountClientContractTest.java in payment-service/src/test/java/com/example/payment/contract/AccountClientContractTest.java
- [ ] T046 [P] [US2] Create application-contract-test.yml in payment-service/src/test/resources/application-contract-test.yml

### Cucumber BDD Tests

- [ ] T047 [P] [US2] Create payment.feature in payment-service/src/test/resources/features/payment.feature
- [ ] T048 [US2] Create PaymentSteps.java in payment-service/src/test/java/com/example/payment/cucumber/PaymentSteps.java
- [ ] T049 [US2] Create CucumberTestRunner.java in payment-service/src/test/java/com/example/payment/cucumber/CucumberTestRunner.java

### Implementation for User Story 2

- [ ] T050 [P] [US2] Create PaymentRequest domain object in payment-service/src/main/java/com/example/payment/domain/PaymentRequest.java
- [ ] T051 [P] [US2] Create AccountDto in payment-service/src/main/java/com/example/payment/infrastructure/client/dto/AccountDto.java
- [ ] T052 [P] [US2] Create DebitRequestDto in payment-service/src/main/java/com/example/payment/infrastructure/client/dto/DebitRequestDto.java
- [ ] T053 [P] [US2] Create DebitResponseDto in payment-service/src/main/java/com/example/payment/infrastructure/client/dto/DebitResponseDto.java
- [ ] T054 [US2] Create AccountClient Feign interface in payment-service/src/main/java/com/example/payment/infrastructure/client/AccountClient.java
- [ ] T055 [US2] Create PaymentService in payment-service/src/main/java/com/example/payment/application/PaymentService.java
- [ ] T056 [US2] Create PaymentController in payment-service/src/main/java/com/example/payment/infrastructure/controller/PaymentController.java
- [ ] T057 [US2] Run `./gradlew :payment-service:test` to validate Consumer contract tests pass with Stub

**Checkpoint**: User Story 2 complete - Consumer can test against Provider Stub independently

---

## Phase 5: User Story 3 - CI/CD 自動化驗證 (Priority: P3)

**Goal**: Gitea Actions 自動執行契約測試、發布 Stub、阻止破壞性變更

**Independent Test**: 推送程式碼後，CI Pipeline 執行並顯示契約測試結果

### Implementation for User Story 3

- [ ] T058 [P] [US3] Create account-service-ci.yaml in .gitea/workflows/account-service-ci.yaml
- [ ] T059 [P] [US3] Create payment-service-ci.yaml in .gitea/workflows/payment-service-ci.yaml
- [ ] T060 [US3] Create contract-verify.yaml scheduled workflow in .gitea/workflows/contract-verify.yaml
- [ ] T061 [US3] Add branch protection rules documentation in docs/ci-cd-setup.md
- [ ] T062 [US3] Test Provider Pipeline by pushing to account-service code
- [ ] T063 [US3] Test Consumer Pipeline by pushing to payment-service code

**Checkpoint**: User Story 3 complete - CI/CD automatically validates contracts

---

## Phase 6: User Story 4 - 契約版本管理與相容性 (Priority: P4)

**Goal**: 管理契約版本，確保 Provider 變更不會破壞現有 Consumer

**Independent Test**: 執行定期契約驗證工作流程，驗證所有 Consumer 仍能正常運作

### Implementation for User Story 4

- [ ] T064 [P] [US4] Add version info to account-service contracts metadata in account-service/src/test/resources/contracts/account/
- [ ] T065 [US4] Configure Stub version management in account-service/build.gradle
- [ ] T066 [US4] Update contract-verify.yaml to check backward compatibility in .gitea/workflows/contract-verify.yaml
- [ ] T067 [US4] Add contract versioning documentation in docs/contract-versioning.md
- [ ] T068 [US4] Test backward compatibility by adding new optional field to contract
- [ ] T069 [US4] Test breaking change detection by removing required field from contract

**Checkpoint**: User Story 4 complete - Contract versioning and backward compatibility verified

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T070 [P] Create account-service/Dockerfile for containerization
- [ ] T071 [P] Create payment-service/Dockerfile for containerization
- [ ] T072 [P] Add structured logging configuration (Logback JSON) in account-service/src/main/resources/logback-spring.xml
- [ ] T073 [P] Add structured logging configuration (Logback JSON) in payment-service/src/main/resources/logback-spring.xml
- [ ] T074 [P] Add Micrometer metrics configuration in account-service/src/main/resources/application.yml
- [ ] T075 [P] Add Micrometer metrics configuration in payment-service/src/main/resources/application.yml
- [ ] T076 Update quickstart.md with actual project paths and commands in specs/001-contract-testing-setup/quickstart.md
- [ ] T077 Run end-to-end validation following quickstart.md steps

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 1 (Phase 3)**: Depends on Foundational - Provider must define contracts first
- **User Story 2 (Phase 4)**: Depends on User Story 1 (needs Provider Stub)
- **User Story 3 (Phase 5)**: Can start after User Story 1 and 2 (CI needs both services)
- **User Story 4 (Phase 6)**: Depends on User Story 3 (versioning needs CI infrastructure)
- **Polish (Phase 7)**: Can start after User Story 1, progresses in parallel with others

### User Story Dependencies

```
Phase 1 (Setup)
     │
     ▼
Phase 2 (Foundational)
     │
     ▼
Phase 3 (US1: Provider Contracts)  ──────────────────┐
     │                                               │
     ▼                                               ▼
Phase 4 (US2: Consumer Tests)      Phase 7 (Polish - partial)
     │
     ▼
Phase 5 (US3: CI/CD)
     │
     ▼
Phase 6 (US4: Versioning)
     │
     ▼
Phase 7 (Polish - complete)
```

### Within Each User Story

- Contract tests MUST be written and FAIL before implementation
- Cucumber feature files before step definitions
- Domain models before services
- Services before controllers
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

**Phase 1 (Setup)**:
- T003, T004 can run in parallel (different modules)
- T005, T006 can run in parallel (different modules)
- T007, T008 can run in parallel (different files)

**Phase 2 (Foundational)**:
- T011, T012 can run in parallel (different modules)
- T013-T019 can run in parallel (different domain classes)
- T021-T023 can run in parallel (different exception classes)

**Phase 3 (US1)**:
- T027-T033 can run in parallel (different contract files)
- T035, T036 can run in parallel (different files)

**Phase 4 (US2)**:
- T045, T046 can run in parallel (different files)
- T047, T048 can run in parallel (different files)
- T050-T053 can run in parallel (different DTO classes)

**Phase 5 (US3)**:
- T058, T059 can run in parallel (different workflow files)

**Phase 7 (Polish)**:
- T070, T071 can run in parallel (different Dockerfiles)
- T072, T073 can run in parallel (different modules)
- T074, T075 can run in parallel (different modules)

---

## Parallel Example: User Story 1 Contracts

```bash
# Launch all contract files for User Story 1 together:
Task: "Create getAccount.groovy contract in account-service/src/test/resources/contracts/account/getAccount.groovy"
Task: "Create getAccountNotFound.groovy contract in account-service/src/test/resources/contracts/account/getAccountNotFound.groovy"
Task: "Create createAccount.groovy contract in account-service/src/test/resources/contracts/account/createAccount.groovy"
Task: "Create debitAccount.groovy contract in account-service/src/test/resources/contracts/account/debitAccount.groovy"
Task: "Create debitInsufficientBalance.groovy contract in account-service/src/test/resources/contracts/account/debitInsufficientBalance.groovy"
Task: "Create freezeAccount.groovy contract in account-service/src/test/resources/contracts/account/freezeAccount.groovy"
Task: "Create unfreezeAccount.groovy contract in account-service/src/test/resources/contracts/account/unfreezeAccount.groovy"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Run `./gradlew :account-service:build`
5. Stub JAR available for Consumers

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Provider contracts ready → **MVP Deliverable!**
3. Add User Story 2 → Consumer can test against Stub → Integration ready
4. Add User Story 3 → CI/CD automation → Production ready
5. Add User Story 4 → Version management → Enterprise ready

### Single Developer Strategy

Execute phases sequentially:
1. Phase 1: Setup (~30 min)
2. Phase 2: Foundational (~1 hour)
3. Phase 3: US1 Provider Contracts (~2 hours)
4. Phase 4: US2 Consumer Tests (~1.5 hours)
5. Phase 5: US3 CI/CD (~1 hour)
6. Phase 6: US4 Versioning (~1 hour)
7. Phase 7: Polish (~1 hour)

---

## Gradle Commands Reference

| 指令 | 說明 |
|------|------|
| `./gradlew build` | 完整建置所有模組含測試 |
| `./gradlew :account-service:build` | 建置 Provider 模組 |
| `./gradlew :payment-service:build` | 建置 Consumer 模組 |
| `./gradlew :account-service:contractTest` | 只執行 Provider 契約測試 |
| `./gradlew :account-service:generateContractTests` | 產生契約測試類別 |
| `./gradlew :account-service:publishToMavenLocal` | 發布 Stub 到本地 Maven |
| `./gradlew :account-service:verifierStubsJar` | 產生 Stub JAR |
| `./gradlew test --tests "*Cucumber*"` | 執行所有 Cucumber 測試 |
| `./gradlew clean` | 清除建置產物 |

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify contract tests fail before implementing controllers
- Verify Cucumber tests fail before implementing step definitions
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Design by Contract: Every contract includes Precondition, Postcondition, Invariant
- BDD: Use Cucumber for acceptance testing with Given-When-Then scenarios
