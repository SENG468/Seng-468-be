package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;

  @Autowired
  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Account addFundsToAccount(Account request) throws EntityMissingException {
    Account account =
        accountRepository.findByName(request.getName()).orElseThrow(EntityMissingException::new);
    account.setBalance(account.getBalance() + request.getBalance());
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
