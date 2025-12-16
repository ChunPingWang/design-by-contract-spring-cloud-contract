package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    name "get_account_not_found"
    description """
        Contract: 查詢不存在的帳戶
        Version: 1.0.0
        Added: 2025-12-16
        Author: Account Service Team

        Precondition: accountId 不存在於系統中
        Postcondition: 返回 404 錯誤，包含錯誤訊息

        Change History:
        - 1.0.0 (2025-12-16): Initial version
    """

    request {
        method GET()
        url "/api/v1/accounts/ACC-999"
    }

    response {
        status NOT_FOUND()
        headers {
            contentType applicationJson()
        }
        body([
            status: 404,
            error: "Not Found",
            message: "Account not found: ACC-999"
        ])
        bodyMatchers {
            jsonPath('$.status', byEquality())
            jsonPath('$.error', byEquality())
            jsonPath('$.message', byRegex('Account not found: .*'))
        }
    }
}
