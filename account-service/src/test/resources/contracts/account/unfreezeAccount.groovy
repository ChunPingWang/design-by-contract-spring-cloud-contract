package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "unfreeze_account_success"
    description """
        Contract: 解凍帳戶成功
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: 帳戶存在且狀態為 FROZEN
        Postcondition: 帳戶狀態變更為 ACTIVE

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method POST()
        url "/api/v1/accounts/ACC-005/unfreeze"
        headers {
            contentType applicationJson()
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-005",
            ownerName: "解凍測試帳戶",
            balance: 2000.00,
            status: "ACTIVE"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byEquality())
            jsonPath('$.status', byEquality())
        }
    }
}
