Feature: 帳戶管理
  作為帳戶服務
  我需要提供帳戶查詢、建立、扣款、凍結、解凍功能
  以便支付服務可以安全地進行交易

  Background:
    Given 帳戶服務已啟動

  @US1 @query
  Scenario: 成功查詢帳戶
    Given 系統中存在帳戶 "ACC-001"，持有人 "王大明"，餘額 10000.00
    When 我查詢帳戶 "ACC-001"
    Then 應該返回帳戶資訊
    And 帳戶餘額應為 10000.00
    And 帳戶狀態應為 "ACTIVE"

  @US1 @query @error
  Scenario: 查詢不存在的帳戶
    Given 系統中不存在帳戶 "ACC-999"
    When 我查詢帳戶 "ACC-999"
    Then 應該返回 404 錯誤
    And 錯誤訊息應包含 "Account not found"

  @US1 @create
  Scenario: 成功建立帳戶
    Given 帳戶 "ACC-NEW" 不存在於系統中
    When 我建立帳戶，帳號 "ACC-NEW"，持有人 "新用戶"，初始餘額 1000.00
    Then 應該成功建立帳戶
    And 新帳戶狀態應為 "ACTIVE"
    And 新帳戶餘額應為 1000.00

  @US1 @debit
  Scenario: 成功扣款
    Given 系統中存在帳戶 "ACC-001"，持有人 "王大明"，餘額 10000.00
    When 我從帳戶 "ACC-001" 扣款 1000.00
    Then 扣款應該成功
    And 帳戶餘額應變為 9000.00
    And 扣款記錄應顯示之前餘額 10000.00

  @US1 @debit @error
  Scenario: 餘額不足扣款失敗
    Given 系統中存在帳戶 "ACC-003"，持有人 "測試用戶"，餘額 500.00
    When 我從帳戶 "ACC-003" 扣款 99999.00
    Then 扣款應該失敗
    And 帳戶餘額應維持 500.00
    And 失敗訊息應為 "Insufficient balance"

  @US1 @freeze
  Scenario: 成功凍結帳戶
    Given 系統中存在帳戶 "ACC-004"，持有人 "凍結測試"，餘額 3000.00，狀態 "ACTIVE"
    When 我凍結帳戶 "ACC-004"，原因 "Suspicious activity"
    Then 帳戶應該成功凍結
    And 帳戶狀態應變為 "FROZEN"

  @US1 @unfreeze
  Scenario: 成功解凍帳戶
    Given 系統中存在帳戶 "ACC-005"，持有人 "解凍測試"，餘額 2000.00，狀態 "FROZEN"
    When 我解凍帳戶 "ACC-005"
    Then 帳戶應該成功解凍
    And 帳戶狀態應變為 "ACTIVE"

  @US1 @debit @freeze @error
  Scenario: 凍結帳戶無法扣款
    Given 系統中存在帳戶 "ACC-006"，持有人 "凍結帳戶"，餘額 5000.00，狀態 "FROZEN"
    When 我從帳戶 "ACC-006" 扣款 100.00
    Then 應該返回 403 錯誤
    And 錯誤訊息應包含 "Account is frozen"

  @US1 @invariant
  Scenario Outline: 餘額不可為負數 (不變量)
    Given 系統中存在帳戶 "<accountNumber>"，持有人 "<owner>"，餘額 <balance>
    When 我從帳戶 "<accountNumber>" 扣款 <debitAmount>
    Then 餘額應該 >= 0

    Examples:
      | accountNumber | owner    | balance  | debitAmount |
      | ACC-INV-001   | 測試者A  | 1000.00  | 1000.00     |
      | ACC-INV-002   | 測試者B  | 500.00   | 499.99      |
