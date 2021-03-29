package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Repositories.*;
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
  private final PendingTransactionRepository pendingTransactionRepository;

  public AdminController(
          UserRepository userRepository,
          TransactionRepository transactionRepository,
          LoggerRepository loggerRepository,
          AccountRepository accountRepository, PendingTransactionRepository pendingTransactionRepository) {
    this.userRepository = userRepository;
    this.transactionRepository = transactionRepository;
    this.loggerRepository = loggerRepository;
    this.accountRepository = accountRepository;

    this.pendingTransactionRepository = pendingTransactionRepository;
  }

  @DeleteMapping("/dump")
  public String dumpDb() {
    userRepository.deleteAll();
    transactionRepository.deleteAll();
    loggerRepository.deleteAll();
    accountRepository.deleteAll();
    pendingTransactionRepository.deleteAll();
    return "Dumped";
  }
}
