package com.example.account.application;

import com.example.account.domain.Account;
import com.example.account.domain.AccountStatus;
import com.example.account.domain.DebitResponse;
import com.example.account.infrastructure.dto.CreateAccountRequest;
import com.example.account.infrastructure.exception.AccountFrozenException;
import com.example.account.infrastructure.exception.AccountNotFoundException;
import com.example.account.infrastructure.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getAccount(String accountNumber) {
        log.debug("Getting account: {}", accountNumber);
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Override
    public Account createAccount(CreateAccountRequest request) {
        log.info("Creating account: {}", request.accountNumber());

        if (accountRepository.existsByAccountNumber(request.accountNumber())) {
            throw new IllegalArgumentException("Account number already exists: " + request.accountNumber());
        }

        Account account = new Account(
                request.accountNumber(),
                request.ownerName(),
                request.initialBalance()
        );

        Account saved = accountRepository.save(account);
        log.info("Account created: {}", saved.getAccountNumber());
        return saved;
    }

    @Override
    public DebitResponse debit(String accountNumber, BigDecimal amount) {
        log.info("Debiting account: {}, amount: {}", accountNumber, amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        // Check if account is frozen
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException(accountNumber);
        }

        BigDecimal previousBalance = account.getBalance();

        boolean success = account.debit(amount);

        if (success) {
            accountRepository.save(account);
            log.info("Debit successful: account={}, previousBalance={}, newBalance={}",
                    accountNumber, previousBalance, account.getBalance());
            return DebitResponse.success(accountNumber, previousBalance, account.getBalance(), amount);
        } else {
            log.warn("Debit failed due to insufficient balance: account={}, balance={}, requested={}",
                    accountNumber, account.getBalance(), amount);
            return DebitResponse.insufficientBalance(accountNumber, account.getBalance(), amount);
        }
    }

    @Override
    public Account freeze(String accountNumber) {
        log.info("Freezing account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.freeze();
        Account saved = accountRepository.save(account);

        log.info("Account frozen: {}", accountNumber);
        return saved;
    }

    @Override
    public Account unfreeze(String accountNumber) {
        log.info("Unfreezing account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        account.unfreeze();
        Account saved = accountRepository.save(account);

        log.info("Account unfrozen: {}", accountNumber);
        return saved;
    }
}
