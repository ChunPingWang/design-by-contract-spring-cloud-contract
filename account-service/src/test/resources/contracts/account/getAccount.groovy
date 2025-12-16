package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "get_account_success"
    description """
        Contract: 查詢帳戶成功
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: accountId 存在於系統中
        Postcondition: 返回帳戶資訊，包含 accountNumber, ownerName, balance, status
        Invariant: balance >= 0

        Change History:
        - 1.0.0 (2025-12-16): Initial version
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
        bodyMatchers {
            jsonPath('$.accountNumber', byRegex('[A-Z]{3}-\\d{3}'))
            jsonPath('$.ownerName', byType())
            jsonPath('$.balance', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.status', byRegex('ACTIVE|FROZEN|CLOSED'))
        }
    }
}
