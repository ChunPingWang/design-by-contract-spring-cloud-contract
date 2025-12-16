# Contract Versioning Guide

## Overview

This document describes the contract versioning strategy for the Spring Cloud Contract based microservices architecture.

## Versioning Strategy

### Semantic Versioning

We use semantic versioning (SemVer) for contract versions:

- **MAJOR**: Breaking changes that require consumer updates
- **MINOR**: New features that are backward compatible
- **PATCH**: Bug fixes that are backward compatible

### Version Format

```
{groupId}:{artifactId}:{version}:stubs
```

Example:
```
com.example:account-service:1.0.0:stubs
```

## Backward Compatibility Rules

### Safe Changes (No Version Bump Required)

1. **Adding new optional fields** to responses
2. **Adding new endpoints** that don't affect existing ones
3. **Making required fields optional** in requests
4. **Adding new enum values** (if consumers handle unknown values gracefully)

### Breaking Changes (MAJOR Version Bump)

1. **Removing fields** from responses
2. **Removing endpoints**
3. **Making optional fields required** in requests
4. **Changing field types**
5. **Changing endpoint URLs**
6. **Changing HTTP methods**
7. **Changing response status codes** for existing scenarios

## Contract Evolution Process

### Step 1: Propose Change

1. Create a new branch for the contract change
2. Update the contract in the provider service
3. Document the change in the contract file comments

### Step 2: Validate Backward Compatibility

```bash
# Run provider contract tests
./gradlew :account-service:contractTest

# Run consumer tests against new stubs
./gradlew :account-service:publishToMavenLocal
./gradlew :payment-service:test
```

### Step 3: Handle Breaking Changes

If the change is breaking:

1. **Deprecation Period**: Keep the old contract for at least one release cycle
2. **Version Bump**: Update the version in `build.gradle`
3. **Consumer Notification**: Notify all consumer teams
4. **Migration Guide**: Provide migration instructions

### Step 4: Release

1. Merge the PR after all consumers confirm compatibility
2. Publish the new stub version
3. Update the contract-verify workflow if needed

## Contract Metadata

### Adding Version Information

Each contract can include metadata for tracking:

```groovy
Contract.make {
    name "get_account_v1"
    description """
        Version: 1.0.0
        Added: 2024-01-15

        Contract for getting account information.

        Breaking changes history:
        - 1.0.0: Initial version
    """
    // ... contract definition
}
```

## Consumer Configuration

### Specifying Version Ranges

Consumers can specify version ranges in their stub configuration:

```groovy
// Use latest version
@AutoConfigureStubRunner(ids = "com.example:account-service:+:stubs")

// Use specific version
@AutoConfigureStubRunner(ids = "com.example:account-service:1.0.0:stubs")

// Use version range
@AutoConfigureStubRunner(ids = "com.example:account-service:[1.0.0,2.0.0):stubs")
```

## CI/CD Integration

### Automatic Compatibility Checks

The `contract-verify.yaml` workflow runs daily to ensure:

1. Provider contracts are valid
2. Stubs can be generated
3. All consumers can use the current stubs

### Breaking Change Detection

When a PR modifies contracts:

1. CI builds new stubs
2. All consumer tests run against new stubs
3. PR is blocked if any consumer test fails

## Troubleshooting

### Consumer Test Failures After Provider Update

1. Check if the provider made breaking changes
2. Review the contract change history
3. Update consumer code if necessary
4. Or request the provider to maintain backward compatibility

### Stub Not Found

```bash
# Verify stub is published
ls ~/.m2/repository/com/example/account-service/

# Re-publish if needed
./gradlew :account-service:publishToMavenLocal
```

### Version Conflicts

If multiple versions of stubs are needed:

1. Use specific version in test configuration
2. Consider running tests against multiple stub versions
3. Coordinate with provider team for version alignment
