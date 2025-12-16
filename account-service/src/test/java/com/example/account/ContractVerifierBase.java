package com.example.account;

import com.example.account.application.AccountService;
import com.example.account.domain.Account;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.controller.AccountController;
import com.example.account.infrastructure.dto.CreateAccountRequest;
import com.example.account.infrastructure.exception.AccountNotFoundException;
import com.example.account.infrastructure.exception.GlobalExceptionHandler;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = {AccountController.class, GlobalExceptionHandler.class})
public abstract class ContractVerifierBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        // Setup test accounts for contracts
        setupGetAccountContract();
        setupGetAccountNotFoundContract();
        setupCreateAccountContract();
        setupDebitAccountContract();
        setupDebitInsufficientBalanceContract();
        setupFreezeAccountContract();
        setupUnfreezeAccountContract();
        setupGetAccountWithCreatedAtContract();
    }

    private void setupGetAccountContract() {
        Account account = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
        account.setId(1L);
        when(accountService.getAccount("ACC-001")).thenReturn(Optional.of(account));
    }

    private void setupGetAccountNotFoundContract() {
        when(accountService.getAccount("ACC-999")).thenReturn(Optional.empty());
    }

    private void setupCreateAccountContract() {
        Account newAccount = new Account("ACC-002", "李小華", new BigDecimal("5000.00"));
        newAccount.setId(2L);
        when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(newAccount);
    }

    private void setupDebitAccountContract() {
        DebitResponse successResponse = DebitResponse.success(
                "ACC-001",
                new BigDecimal("10000.00"),
                new BigDecimal("9000.00"),
                new BigDecimal("1000.00")
        );
        when(accountService.debit(eq("ACC-001"), eq(new BigDecimal("1000.00")))).thenReturn(successResponse);
    }

    private void setupDebitInsufficientBalanceContract() {
        DebitResponse insufficientResponse = DebitResponse.insufficientBalance(
                "ACC-003",
                new BigDecimal("500.00"),
                new BigDecimal("99999.00")
        );
        when(accountService.debit(eq("ACC-003"), eq(new BigDecimal("99999.00")))).thenReturn(insufficientResponse);
    }

    private void setupFreezeAccountContract() {
        Account frozenAccount = new Account("ACC-004", "凍結測試帳戶", new BigDecimal("3000.00"));
        frozenAccount.setId(4L);
        frozenAccount.freeze();
        when(accountService.freeze("ACC-004")).thenReturn(frozenAccount);
    }

    private void setupUnfreezeAccountContract() {
        // Create account, freeze it, then unfreeze
        Account unfrozenAccount = new Account("ACC-005", "解凍測試帳戶", new BigDecimal("2000.00"));
        unfrozenAccount.setId(5L);
        // Account is already ACTIVE after unfreeze
        when(accountService.unfreeze("ACC-005")).thenReturn(unfrozenAccount);
    }

    private void setupGetAccountWithCreatedAtContract() {
        // Test backward compatibility - new optional field
        Account account = new Account("ACC-006", "向後相容測試", new BigDecimal("8000.00"));
        account.setId(6L);
        when(accountService.getAccount("ACC-006")).thenReturn(Optional.of(account));
    }
}
