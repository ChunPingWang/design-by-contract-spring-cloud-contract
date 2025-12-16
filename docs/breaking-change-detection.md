# Breaking Change Detection Guide

## Overview

This guide explains how to detect and handle breaking changes in contracts using Spring Cloud Contract.

## What is a Breaking Change?

A breaking change is any modification to a contract that would cause existing consumers to fail. Examples include:

1. **Removing required fields** from responses
2. **Changing field types** (e.g., `String` to `Integer`)
3. **Renaming fields** without maintaining backward compatibility
4. **Changing HTTP status codes** for existing scenarios
5. **Modifying endpoint URLs**
6. **Changing request requirements** (making optional fields required)

## Detection Workflow

### Step 1: Make the Breaking Change (Example)

Let's simulate removing the `ownerName` field from the `getAccount` response:

```groovy
// BREAKING CHANGE: Removed 'ownerName' field
Contract.make {
    name "get_account_success_v2_breaking"
    description """
        WARNING: This is a BREAKING CHANGE!
        Version: 2.0.0 (Breaking)

        Change: Removed 'ownerName' field from response
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-001"
    }

    response {
        status OK()
        body([
            accountNumber: "ACC-001",
            // REMOVED: ownerName
            balance: 10000.00,
            status: "ACTIVE"
        ])
    }
}
```

### Step 2: Run Provider Contract Tests

```bash
./gradlew :account-service:contractTest
```

If the provider implementation still includes the field, the contract test will pass, but this creates a mismatch.

### Step 3: Run Consumer Contract Tests

```bash
# First, publish the new stubs
./gradlew :account-service:publishToMavenLocal

# Then run consumer tests
./gradlew :payment-service:test
```

**Expected Result**: The consumer tests should FAIL because they expect the `ownerName` field.

### Step 4: Detection in CI/CD

The `contract-verify.yaml` workflow detects breaking changes automatically:

```yaml
- name: Verify Consumer can use Provider contracts
  run: ./gradlew :payment-service:test
```

If this step fails after a provider change, it indicates a breaking change.

## Safe vs Breaking Changes

### Safe Changes (No Consumer Update Required)

| Change Type | Example | Why Safe |
|-------------|---------|----------|
| Add optional field | Add `createdAt` to response | Consumers ignore unknown fields |
| Add new endpoint | Add `/api/v1/accounts/{id}/history` | Doesn't affect existing endpoints |
| Make required field optional | `reason` no longer required in freeze request | Consumers can still send it |
| Add new enum value | Add `SUSPENDED` to `AccountStatus` | Consumers handle unknown values |

### Breaking Changes (Consumer Update Required)

| Change Type | Example | Why Breaking |
|-------------|---------|--------------|
| Remove field | Remove `ownerName` | Consumers expect this field |
| Change type | `balance: String` → `balance: Number` | Type mismatch |
| Rename field | `ownerName` → `accountHolder` | Field not found |
| Change URL | `/accounts/{id}` → `/v2/accounts/{id}` | Endpoint not found |
| Make optional required | `description` now required in request | Existing calls fail |

## Handling Breaking Changes

### Option 1: Deprecation Period

1. Keep the old contract
2. Add new contract with changes
3. Notify consumers
4. Remove old contract after migration

```groovy
// Deprecated contract (v1)
Contract.make {
    name "get_account_v1_deprecated"
    description """
        DEPRECATED: Will be removed in v3.0.0
        Use get_account_v2 instead
    """
    // ... old contract
}

// New contract (v2)
Contract.make {
    name "get_account_v2"
    description """
        Version: 2.0.0
        Replaces: get_account_v1 (deprecated)
    """
    // ... new contract
}
```

### Option 2: Version in URL

```groovy
// V1 endpoint (maintained for compatibility)
Contract.make {
    name "get_account_v1"
    request {
        url "/api/v1/accounts/ACC-001"
    }
    // ... v1 response
}

// V2 endpoint (new version)
Contract.make {
    name "get_account_v2"
    request {
        url "/api/v2/accounts/ACC-001"
    }
    // ... v2 response
}
```

### Option 3: Content Negotiation

Use Accept header for version negotiation:

```groovy
Contract.make {
    name "get_account_v2_content_negotiation"
    request {
        headers {
            accept("application/vnd.account.v2+json")
        }
    }
    // ... v2 response
}
```

## Best Practices

1. **Always run consumer tests** after provider changes
2. **Use CI/CD** to automatically detect breaking changes
3. **Document all changes** in contract descriptions
4. **Communicate with consumers** before making breaking changes
5. **Use semantic versioning** for stubs
6. **Maintain a deprecation policy** (e.g., 2 release cycles)

## Testing Breaking Change Detection

To verify your CI/CD properly detects breaking changes:

1. Create a temporary breaking change in a branch
2. Run the full build pipeline
3. Verify consumer tests fail
4. Document the failure message
5. Revert the breaking change

Example test command:

```bash
# This should FAIL if contract-verify is working correctly
./gradlew clean build
```

## Recovery from Accidental Breaking Changes

1. **Immediate**: Revert the breaking change
2. **Short-term**: Fix consumer code if revert not possible
3. **Long-term**: Implement proper deprecation workflow
