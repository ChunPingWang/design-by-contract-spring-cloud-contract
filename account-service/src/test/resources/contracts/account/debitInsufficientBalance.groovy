package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "debit_account_insufficient_balance"
    description """
        Contract: 帳戶扣款失敗 - 餘額不足
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition:
        - accountId 存在
        - amount > balance (餘額不足)

        Postcondition: 返回失敗訊息，餘額不變

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method POST()
        url "/api/v1/accounts/ACC-003/debit"
        headers {
            contentType applicationJson()
        }
        body([
            amount: 99999.00
        ])
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            accountNumber: "ACC-003",
            previousBalance: 500.00,
            currentBalance: 500.00,
            debitedAmount: 99999.00,
            success: false,
            message: "Insufficient balance"
        ])
        bodyMatchers {
            jsonPath('$.accountNumber', byEquality())
            jsonPath('$.success', byEquality())
            jsonPath('$.message', byEquality())
        }
    }
}
