package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "create_account_success"
    description """
        Contract: 建立帳戶成功
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: accountNumber 唯一，ownerName 不為空，initialBalance >= 0
        Postcondition: 返回新建立的帳戶，狀態為 ACTIVE
        Invariant: balance >= 0

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method POST()
        url "/api/v1/accounts"
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-002",
            ownerName: "李小華",
            initialBalance: 5000.00
        ])
    }

    response {
        status CREATED()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-002",
            ownerName: "李小華",
            balance: 5000.00,
            status: "ACTIVE"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byEquality())
            jsonPath('$.ownerName', byEquality())
            jsonPath('$.balance', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.status', byEquality())
        }
    }
}
