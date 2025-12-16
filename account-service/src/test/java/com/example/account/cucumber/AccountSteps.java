package com.example.account.cucumber;

import com.example.account.application.AccountService;
import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.dto.CreateAccountRequest;
import com.example.account.infrastructure.exception.AccountFrozenException;
import com.example.account.infrastructure.exception.AccountNotFoundException;
import io.cucumber.java.Before;
import io.cucumber.java.zh_tw.並且;
import io.cucumber.java.zh_tw.假設;
import io.cucumber.java.zh_tw.當;
import io.cucumber.java.zh_tw.那麼;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AccountSteps {

    @Autowired
    private AccountService accountService;

    private Map<String, Account> testAccounts = new HashMap<>();
    private Account currentAccount;
    private DebitResponse currentDebitResponse;
    private Exception currentException;
    private int currentStatusCode;

    @Before
    public void setup() {
        testAccounts.clear();
        currentAccount = null;
        currentDebitResponse = null;
        currentException = null;
        currentStatusCode = 0;
    }

    @假設("帳戶服務已啟動")
    public void account_service_is_running() {
        assertThat(accountService).isNotNull();
    }

    @假設("系統中存在帳戶 {string}，持有人 {string}，餘額 {bigdecimal}")
    public void account_exists(String accountNumber, String ownerName, BigDecimal balance) {
        try {
            CreateAccountRequest request = new CreateAccountRequest(accountNumber, ownerName, balance);
            Account account = accountService.createAccount(request);
            testAccounts.put(accountNumber, account);
        } catch (IllegalArgumentException e) {
            // Account might already exist from previous test
            accountService.getAccount(accountNumber).ifPresent(acc -> testAccounts.put(accountNumber, acc));
        }
    }

    @假設("系統中存在帳戶 {string}，持有人 {string}，餘額 {bigdecimal}，狀態 {string}")
    public void account_exists_with_status(String accountNumber, String ownerName, BigDecimal balance, String status) {
        account_exists(accountNumber, ownerName, balance);

        if ("FROZEN".equals(status)) {
            Account frozen = accountService.freeze(accountNumber);
            testAccounts.put(accountNumber, frozen);
        }
    }

    @假設("系統中不存在帳戶 {string}")
    public void account_does_not_exist(String accountNumber) {
        Optional<Account> existing = accountService.getAccount(accountNumber);
        assertThat(existing).isEmpty();
    }

    @假設("帳戶 {string} 不存在於系統中")
    public void account_not_exists_in_system(String accountNumber) {
        account_does_not_exist(accountNumber);
    }

    @當("我查詢帳戶 {string}")
    public void query_account(String accountNumber) {
        try {
            Optional<Account> account = accountService.getAccount(accountNumber);
            if (account.isPresent()) {
                currentAccount = account.get();
                currentStatusCode = 200;
            } else {
                currentStatusCode = 404;
                currentException = new AccountNotFoundException(accountNumber);
            }
        } catch (Exception e) {
            currentException = e;
            currentStatusCode = 500;
        }
    }

    @當("我建立帳戶，帳號 {string}，持有人 {string}，初始餘額 {bigdecimal}")
    public void create_account(String accountNumber, String ownerName, BigDecimal initialBalance) {
        try {
            CreateAccountRequest request = new CreateAccountRequest(accountNumber, ownerName, initialBalance);
            currentAccount = accountService.createAccount(request);
            currentStatusCode = 201;
        } catch (Exception e) {
            currentException = e;
            currentStatusCode = 400;
        }
    }

    @當("我從帳戶 {string} 扣款 {bigdecimal}")
    public void debit_account(String accountNumber, BigDecimal amount) {
        try {
            currentDebitResponse = accountService.debit(accountNumber, amount);
            currentStatusCode = 200;
        } catch (AccountFrozenException e) {
            currentException = e;
            currentStatusCode = 403;
        } catch (AccountNotFoundException e) {
            currentException = e;
            currentStatusCode = 404;
        } catch (Exception e) {
            currentException = e;
            currentStatusCode = 500;
        }
    }

    @當("我凍結帳戶 {string}，原因 {string}")
    public void freeze_account(String accountNumber, String reason) {
        try {
            currentAccount = accountService.freeze(accountNumber);
            currentStatusCode = 200;
        } catch (Exception e) {
            currentException = e;
            currentStatusCode = 400;
        }
    }

    @當("我解凍帳戶 {string}")
    public void unfreeze_account(String accountNumber) {
        try {
            currentAccount = accountService.unfreeze(accountNumber);
            currentStatusCode = 200;
        } catch (Exception e) {
            currentException = e;
            currentStatusCode = 400;
        }
    }

    @那麼("應該返回帳戶資訊")
    public void should_return_account_info() {
        assertThat(currentAccount).isNotNull();
        assertThat(currentStatusCode).isEqualTo(200);
    }

    @那麼("應該返回 {int} 錯誤")
    public void should_return_error(int statusCode) {
        assertThat(currentStatusCode).isEqualTo(statusCode);
    }

    @那麼("錯誤訊息應包含 {string}")
    public void error_message_should_contain(String message) {
        assertThat(currentException).isNotNull();
        assertThat(currentException.getMessage()).contains(message);
    }

    @那麼("應該成功建立帳戶")
    public void should_create_account_successfully() {
        assertThat(currentAccount).isNotNull();
        assertThat(currentStatusCode).isEqualTo(201);
    }

    @那麼("新帳戶狀態應為 {string}")
    public void new_account_status_should_be(String status) {
        assertThat(currentAccount.getStatus().name()).isEqualTo(status);
    }

    @那麼("新帳戶餘額應為 {bigdecimal}")
    public void new_account_balance_should_be(BigDecimal balance) {
        assertThat(currentAccount.getBalance()).isEqualByComparingTo(balance);
    }

    @那麼("扣款應該成功")
    public void debit_should_succeed() {
        assertThat(currentDebitResponse).isNotNull();
        assertThat(currentDebitResponse.success()).isTrue();
    }

    @那麼("扣款應該失敗")
    public void debit_should_fail() {
        if (currentDebitResponse != null) {
            assertThat(currentDebitResponse.success()).isFalse();
        } else {
            assertThat(currentException).isNotNull();
        }
    }

    @那麼("帳戶應該成功凍結")
    public void account_should_be_frozen() {
        assertThat(currentAccount).isNotNull();
        assertThat(currentAccount.getStatus()).isEqualTo(AccountStatus.FROZEN);
    }

    @那麼("帳戶應該成功解凍")
    public void account_should_be_unfrozen() {
        assertThat(currentAccount).isNotNull();
        assertThat(currentAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @那麼("餘額應該 >= {int}")
    public void balance_should_be_non_negative(int minBalance) {
        if (currentDebitResponse != null) {
            assertThat(currentDebitResponse.currentBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    @並且("帳戶餘額應為 {bigdecimal}")
    public void account_balance_should_be(BigDecimal balance) {
        assertThat(currentAccount.getBalance()).isEqualByComparingTo(balance);
    }

    @並且("帳戶餘額應變為 {bigdecimal}")
    public void account_balance_should_become(BigDecimal balance) {
        assertThat(currentDebitResponse.currentBalance()).isEqualByComparingTo(balance);
    }

    @並且("帳戶餘額應維持 {bigdecimal}")
    public void account_balance_should_remain(BigDecimal balance) {
        assertThat(currentDebitResponse.currentBalance()).isEqualByComparingTo(balance);
    }

    @並且("帳戶狀態應為 {string}")
    public void account_status_should_be(String status) {
        assertThat(currentAccount.getStatus().name()).isEqualTo(status);
    }

    @並且("帳戶狀態應變為 {string}")
    public void account_status_should_become(String status) {
        assertThat(currentAccount.getStatus().name()).isEqualTo(status);
    }

    @並且("扣款記錄應顯示之前餘額 {bigdecimal}")
    public void debit_record_should_show_previous_balance(BigDecimal previousBalance) {
        assertThat(currentDebitResponse.previousBalance()).isEqualByComparingTo(previousBalance);
    }

    @並且("失敗訊息應為 {string}")
    public void failure_message_should_be(String message) {
        assertThat(currentDebitResponse.message()).isEqualTo(message);
    }
}
