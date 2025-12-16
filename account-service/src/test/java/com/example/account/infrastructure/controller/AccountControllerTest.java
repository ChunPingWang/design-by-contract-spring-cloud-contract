package com.example.account.infrastructure.controller;

import com.example.account.application.AccountService;
import com.example.account.domain.Account;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.dto.CreateAccountRequest;
import com.example.account.infrastructure.exception.AccountNotFoundException;
import com.example.account.infrastructure.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AccountController.class, GlobalExceptionHandler.class})
@DisplayName("AccountController Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
        testAccount.setId(1L);
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{accountNumber}")
    class GetAccount {

        @Test
        @DisplayName("should return account when found")
        void shouldReturnAccountWhenFound() throws Exception {
            when(accountService.getAccount("ACC-001")).thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/api/v1/accounts/ACC-001"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.ownerName").value("王大明"))
                    .andExpect(jsonPath("$.balance").value(10000.00))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("should return 404 when account not found")
        void shouldReturn404WhenAccountNotFound() throws Exception {
            when(accountService.getAccount("ACC-999")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/accounts/ACC-999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts")
    class CreateAccount {

        @Test
        @DisplayName("should create account successfully")
        void shouldCreateAccountSuccessfully() throws Exception {
            Account newAccount = new Account("ACC-002", "李小華", new BigDecimal("5000.00"));
            newAccount.setId(2L);
            when(accountService.createAccount(any(CreateAccountRequest.class))).thenReturn(newAccount);

            String requestBody = """
                    {
                        "accountNumber": "ACC-002",
                        "ownerName": "李小華",
                        "initialBalance": 5000.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-002"))
                    .andExpect(jsonPath("$.ownerName").value("李小華"))
                    .andExpect(jsonPath("$.balance").value(5000.00))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("should return 400 for invalid request - missing fields")
        void shouldReturn400ForInvalidRequest() throws Exception {
            String requestBody = """
                    {
                        "accountNumber": ""
                    }
                    """;

            mockMvc.perform(post("/api/v1/accounts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/{accountNumber}/debit")
    class DebitAccount {

        @Test
        @DisplayName("should debit account successfully")
        void shouldDebitAccountSuccessfully() throws Exception {
            DebitResponse response = DebitResponse.success(
                    "ACC-001",
                    new BigDecimal("10000.00"),
                    new BigDecimal("9000.00"),
                    new BigDecimal("1000.00")
            );
            when(accountService.debit(eq("ACC-001"), eq(new BigDecimal("1000.00")))).thenReturn(response);

            String requestBody = """
                    {
                        "amount": 1000.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/accounts/ACC-001/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.previousBalance").value(10000.00))
                    .andExpect(jsonPath("$.currentBalance").value(9000.00));
        }

        @Test
        @DisplayName("should return response for insufficient balance")
        void shouldReturnResponseForInsufficientBalance() throws Exception {
            DebitResponse response = DebitResponse.insufficientBalance(
                    "ACC-001",
                    new BigDecimal("500.00"),
                    new BigDecimal("1000.00")
            );
            when(accountService.debit(eq("ACC-001"), eq(new BigDecimal("1000.00")))).thenReturn(response);

            String requestBody = """
                    {
                        "amount": 1000.00
                    }
                    """;

            mockMvc.perform(post("/api/v1/accounts/ACC-001/debit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Insufficient balance"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/{accountNumber}/freeze")
    class FreezeAccount {

        @Test
        @DisplayName("should freeze account successfully")
        void shouldFreezeAccountSuccessfully() throws Exception {
            Account frozenAccount = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            frozenAccount.setId(1L);
            frozenAccount.freeze();
            when(accountService.freeze("ACC-001")).thenReturn(frozenAccount);

            mockMvc.perform(post("/api/v1/accounts/ACC-001/freeze")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.status").value("FROZEN"));
        }

        @Test
        @DisplayName("should freeze account with optional request body")
        void shouldFreezeAccountWithOptionalRequestBody() throws Exception {
            Account frozenAccount = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            frozenAccount.setId(1L);
            frozenAccount.freeze();
            when(accountService.freeze("ACC-001")).thenReturn(frozenAccount);

            String requestBody = """
                    {
                        "reason": "Suspicious activity"
                    }
                    """;

            mockMvc.perform(post("/api/v1/accounts/ACC-001/freeze")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FROZEN"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/{accountNumber}/unfreeze")
    class UnfreezeAccount {

        @Test
        @DisplayName("should unfreeze account successfully")
        void shouldUnfreezeAccountSuccessfully() throws Exception {
            Account unfrozenAccount = new Account("ACC-001", "王大明", new BigDecimal("10000.00"));
            unfrozenAccount.setId(1L);
            when(accountService.unfreeze("ACC-001")).thenReturn(unfrozenAccount);

            mockMvc.perform(post("/api/v1/accounts/ACC-001/unfreeze")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("AccountResponse DTO Tests")
    class AccountResponseTests {

        @Test
        @DisplayName("should include createdAt field when present")
        void shouldIncludeCreatedAtFieldWhenPresent() throws Exception {
            when(accountService.getAccount("ACC-001")).thenReturn(Optional.of(testAccount));

            mockMvc.perform(get("/api/v1/accounts/ACC-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdAt").exists());
        }
    }
}
