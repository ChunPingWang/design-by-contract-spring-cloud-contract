package com.example.payment.cucumber;

import com.example.payment.application.PaymentService;
import com.example.payment.application.PaymentService.PaymentResult;
import com.example.payment.domain.PaymentRequest;
import com.example.payment.infrastructure.client.dto.AccountDto;
import io.cucumber.java.Before;
import io.cucumber.java.zh_tw.並且;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "account-service.url=http://localhost:6565"
)
@AutoConfigureStubRunner(
        ids = "com.example:account-service:+:stubs:6565",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class PaymentSteps {

    @Autowired
    private PaymentService paymentService;

    private AccountDto currentAccount;
    private PaymentResult currentPaymentResult;
    private Exception currentException;

    @Before
    public void setup() {
        currentAccount = null;
        currentPaymentResult = null;
        currentException = null;
    }

    @假設("支付服務已啟動")
    public void payment_service_is_running() {
        assertThat(paymentService).isNotNull();
    }

    @假設("帳戶服務 Stub 已啟動")
    public void account_service_stub_is_running() {
        // Stub Runner auto-configures the stub
    }

    @假設("帳戶 {string} 存在且餘額為 {bigdecimal}")
    public void account_exists_with_balance(String accountNumber, BigDecimal balance) {
        // This is verified by the stub - no action needed
        // The stub will respond according to the contracts
    }

    @當("我發起支付請求，帳戶 {string}，金額 {bigdecimal}")
    public void process_payment(String accountNumber, BigDecimal amount) {
        try {
            PaymentRequest request = new PaymentRequest(accountNumber, amount, "Cucumber test payment");
            currentPaymentResult = paymentService.processPayment(request);
        } catch (Exception e) {
            currentException = e;
        }
    }

    @當("我查詢帳戶 {string} 資訊")
    public void query_account(String accountNumber) {
        try {
            currentAccount = paymentService.getAccount(accountNumber);
        } catch (Exception e) {
            currentException = e;
        }
    }

    @那麼("支付應該成功")
    public void payment_should_succeed() {
        assertThat(currentPaymentResult).isNotNull();
        assertThat(currentPaymentResult.status()).isEqualTo(PaymentResult.Status.SUCCESS);
    }

    @那麼("支付應該失敗")
    public void payment_should_fail() {
        assertThat(currentPaymentResult).isNotNull();
        assertThat(currentPaymentResult.status()).isEqualTo(PaymentResult.Status.FAILED);
    }

    @那麼("應該返回帳戶詳情")
    public void should_return_account_details() {
        assertThat(currentAccount).isNotNull();
    }

    @並且("扣款後餘額應為 {bigdecimal}")
    public void balance_after_debit_should_be(BigDecimal balance) {
        assertThat(currentPaymentResult.currentBalance()).isEqualByComparingTo(balance);
    }

    @並且("失敗原因應為 {string}")
    public void failure_reason_should_be(String reason) {
        assertThat(currentPaymentResult.message()).isEqualTo(reason);
    }

    @並且("帳戶餘額應為 {bigdecimal}")
    public void account_balance_should_be(BigDecimal balance) {
        assertThat(currentAccount.balance()).isEqualByComparingTo(balance);
    }

    @並且("帳戶狀態應為 {string}")
    public void account_status_should_be(String status) {
        assertThat(currentAccount.status()).isEqualTo(status);
    }
}
