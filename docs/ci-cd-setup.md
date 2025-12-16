# CI/CD Setup Guide

## Overview

This document describes the CI/CD pipeline setup for contract testing with Gitea Actions.

## Pipeline Architecture

```
                    ┌─────────────────┐
                    │   Code Push     │
                    └────────┬────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
              ▼              ▼              ▼
     ┌────────────┐  ┌────────────┐  ┌────────────┐
     │  Account   │  │  Payment   │  │  Contract  │
     │  Service   │  │  Service   │  │  Verify    │
     │    CI      │  │    CI      │  │ (Scheduled)│
     └─────┬──────┘  └─────┬──────┘  └─────┬──────┘
           │               │               │
           ▼               ▼               ▼
     ┌──────────┐    ┌──────────┐    ┌──────────┐
     │  Build   │    │  Build   │    │  Full    │
     │  Test    │    │  Test    │    │  Verify  │
     │  Publish │    │  Contract│    │  Report  │
     └──────────┘    └──────────┘    └──────────┘
```

## Workflows

### 1. Account Service CI (`account-service-ci.yaml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main`
- Only when account-service files change

**Jobs:**

1. **build-and-test**
   - Checkout code
   - Setup JDK 17
   - Build account-service
   - Run contract tests
   - Generate stub JAR
   - Upload artifacts

2. **publish-stubs** (main branch only)
   - Publish stubs to Maven repository

### 2. Payment Service CI (`payment-service-ci.yaml`)

**Triggers:**
- Push to `main` or `develop` branches
- Pull requests to `main`
- Only when payment-service files change

**Jobs:**

1. **contract-test**
   - Build account-service stubs first
   - Build payment-service
   - Run contract tests against stubs
   - Upload test reports

2. **integration-test** (main branch only)
   - Run integration tests (if configured)

### 3. Contract Verification (`contract-verify.yaml`)

**Triggers:**
- Daily at midnight UTC
- Manual trigger

**Purpose:**
- Verify all contracts are still valid
- Ensure consumers can use provider stubs
- Generate verification report

## Setup Instructions

### Prerequisites

1. **Gitea Actions Runner**
   - Ensure Gitea Actions is enabled
   - Runner has JDK 17 available

2. **Repository Settings**
   - Enable Actions in repository settings
   - Configure branch protection if needed

### Branch Protection Rules

Recommended branch protection for `main`:

1. **Require PR reviews**: At least 1 approval
2. **Require status checks**:
   - `build-and-test` (account-service-ci)
   - `contract-test` (payment-service-ci)
3. **Require branches to be up to date**
4. **Block force pushes**

### Secrets Configuration

If publishing to a remote Maven repository, configure:

- `MAVEN_USERNAME`: Maven repository username
- `MAVEN_PASSWORD`: Maven repository password

## Workflow Files Location

```
.gitea/
└── workflows/
    ├── account-service-ci.yaml
    ├── payment-service-ci.yaml
    └── contract-verify.yaml
```

## Monitoring

### Viewing Pipeline Results

1. Go to repository → Actions tab
2. Select the workflow run
3. View job logs and artifacts

### Artifacts

Each workflow produces artifacts:

| Workflow | Artifact | Contents |
|----------|----------|----------|
| account-service-ci | test-reports | Test reports |
| account-service-ci | account-service-stubs | Stub JAR |
| payment-service-ci | payment-test-reports | Test reports |
| contract-verify | contract-verification-report | Verification report |

## Troubleshooting

### Build Failures

1. **Java version mismatch**
   - Ensure JDK 17 is used
   - Check `setup-java` action configuration

2. **Gradle cache issues**
   - Clear cache: Re-run with `--no-cache`

3. **Test failures**
   - Check test reports in artifacts
   - Verify contract compatibility

### Stub Publishing Issues

1. **Authentication failed**
   - Verify Maven credentials
   - Check repository URL

2. **Stub not found by consumer**
   - Ensure provider CI ran first
   - Verify stub is in Maven repository

## Best Practices

1. **Run provider CI before consumer CI**
   - Provider must publish stubs first
   - Use workflow dependencies if needed

2. **Keep contracts backward compatible**
   - Add new fields as optional
   - Deprecate before removing

3. **Monitor scheduled verification**
   - Review daily verification reports
   - Fix issues promptly

4. **Use specific stub versions in production**
   - Avoid `+` in production consumer tests
   - Pin to known-good versions
