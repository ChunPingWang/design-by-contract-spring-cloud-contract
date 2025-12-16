package contracts.account

import org.springframework.cloud.contract.spec.Contract

/**
 * This contract demonstrates backward compatibility by adding a new optional field.
 *
 * The 'createdAt' field is added in version 1.1.0 as an optional field.
 * Existing consumers that don't expect this field will continue to work
 * because they simply ignore unknown fields.
 *
 * This is a SAFE change that doesn't require consumer updates.
 */
Contract.make {
    name "get_account_with_created_at"
    description """
        Contract: 查詢帳戶（含建立時間）
        Version: 1.1.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: accountId 存在於系統中
        Postcondition: 返回帳戶資訊，包含 accountNumber, ownerName, balance, status, createdAt (新增)
        Invariant: balance >= 0

        BACKWARD COMPATIBLE: 新增可選欄位 createdAt
        - 現有 Consumer 不需要更新
        - 新 Consumer 可以選擇使用此欄位

        Change History:
        - 1.0.0 (2025-12-16): Initial version
        - 1.1.0 (2025-12-16): Added optional 'createdAt' field (backward compatible)
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-006"
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-006",
            ownerName: "向後相容測試",
            balance: 8000.00,
            status: "ACTIVE",
            // New optional field - existing consumers will ignore this
            createdAt: "2025-12-16T10:00:00"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byRegex('[A-Z]{3}-\\d{3}'))
            jsonPath('$.ownerName', byType())
            jsonPath('$.balance', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.status', byRegex('ACTIVE|FROZEN|CLOSED'))
            // New optional field matcher
            jsonPath('$.createdAt', byType())
        }
    }
}
