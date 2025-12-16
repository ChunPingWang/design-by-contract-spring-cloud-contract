Feature: 支付服務
  作為支付服務
  我需要調用帳戶服務進行扣款
  以便完成支付交易

  Background:
    Given 支付服務已啟動
    And 帳戶服務 Stub 已啟動

  @US2 @payment
  Scenario: 成功處理支付
    Given 帳戶 "ACC-001" 存在且餘額為 10000.00
    When 我發起支付請求，帳戶 "ACC-001"，金額 1000.00
    Then 支付應該成功
    And 扣款後餘額應為 9000.00

  @US2 @query
  Scenario: 查詢帳戶資訊
    Given 帳戶 "ACC-001" 存在且餘額為 10000.00
    When 我查詢帳戶 "ACC-001" 資訊
    Then 應該返回帳戶詳情
    And 帳戶餘額應為 10000.00
    And 帳戶狀態應為 "ACTIVE"
