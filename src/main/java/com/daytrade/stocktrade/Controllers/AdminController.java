package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Repositories.AccountRepository;
import com.daytrade.stocktrade.Repositories.LoggerRepository;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import com.daytrade.stocktrade.Repositories.UserRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;
  private final LoggerRepository loggerRepository;
  private final AccountRepository accountRepository;

  public AdminController(
      UserRepository userRepository,
      TransactionRepository transactionRepository,
      LoggerRepository loggerRepository,
      AccountRepository accountRepository) {
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.loggerRepository = loggerRepository;
    this.accountRepository = accountRepository;
  }

  @DeleteMapping("/dump")
  public String dumpDb() {
    userRepository.deleteAll();
    transactionRepository.deleteAll();
    loggerRepository.deleteAll();
    accountRepository.deleteAll();
    return "Dumped";
  }
}
