package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final LoggerService loggerService;

  @Autowired
  public AccountService(AccountRepository accountRepository, LoggerService loggerService) {
    this.accountRepository = accountRepository;
    this.loggerService = loggerService;
  }

  public Account addFundsToAccount(Account request) throws EntityMissingException {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    if (request.getBalance() < 0) {
      throw new BadRequestException("You cannot add negative money to an account");
    }
    Account account = accountRepository.findByName(name).orElseThrow(EntityMissingException::new);
    account.setBalance(account.getBalance() + request.getBalance());

    loggerService.createAccountTransactionLog(name, request.getId(), "add", request.getBalance());
    account.setName(name);
    return accountRepository.save(account);
  }

  public Account getByName(String name) throws EntityMissingException {
    return accountRepository.findByName(name).orElseThrow(EntityMissingException::new);
  }

  public Account createNewAccount(String username) {
    return accountRepository.save(new Account(username));
  }

  public Account save(Account account) {
    return accountRepository.save(account);
  }
}
