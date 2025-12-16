package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "freeze_account_success"
    description """
        Contract: 凍結帳戶成功
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: 帳戶存在且狀態為 ACTIVE
        Postcondition: 帳戶狀態變更為 FROZEN

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method POST()
        url "/api/v1/accounts/ACC-004/freeze"
        headers {
            contentType applicationJson()
        }
        body([
            reason: "Suspicious activity detected"
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-004",
            ownerName: "凍結測試帳戶",
            balance: 3000.00,
            status: "FROZEN"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byEquality())
            jsonPath('$.status', byEquality())
        }
    }
}
