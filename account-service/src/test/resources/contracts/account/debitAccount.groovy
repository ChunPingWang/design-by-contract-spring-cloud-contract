package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "debit_account_success"
    description """
        Contract: 帳戶扣款成功
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition:
        - accountId 存在
        - amount > 0
        - amount <= balance
        - 帳戶狀態為 ACTIVE

        Postcondition: newBalance = oldBalance - amount
        Invariant: balance >= 0

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method POST()
        url "/api/v1/accounts/ACC-001/debit"
        headers {
            contentType applicationJson()
        }
        body([
            amount: 1000.00
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-001",
            previousBalance: 10000.00,
            currentBalance: 9000.00,
            debitedAmount: 1000.00,
            success: true,
            message: "Debit successful"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byEquality())
            jsonPath('$.previousBalance', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.currentBalance', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.debitedAmount', byRegex('[0-9]+\\.?[0-9]*'))
            jsonPath('$.success', byEquality())
            jsonPath('$.message', byEquality())
        }
    }
}
