package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Summary;
import com.daytrade.stocktrade.Models.Transactions.PendingTransaction;
import com.daytrade.stocktrade.Models.Transactions.Transaction;
import com.daytrade.stocktrade.Repositories.AccountRepository;
import com.daytrade.stocktrade.Repositories.PendingTransactionRepository;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final PendingTransactionRepository pendingTransactionRepository;
  private final LoggerService loggerService;
  private final Boolean debug;

  @Autowired
  public AccountService(
      AccountRepository accountRepository,
      LoggerService loggerService,
      TransactionRepository transactionRepository,
      PendingTransactionRepository pendingTransactionRepository,
      @Value("${security.debug}") Boolean debug) {
    this.accountRepository = accountRepository;
    this.loggerService = loggerService;
    this.transactionRepository = transactionRepository;
    this.pendingTransactionRepository = pendingTransactionRepository;
    this.debug = debug;
  }

  public Account addFundsToAccount(Account request) throws EntityMissingException {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    if (request.getBalance() < 0) {
      loggerService.createErrorEventLog(
          name,
          request.getTransactionId(),
          Enums.CommandType.ADD,
          null,
          null,
          null,
          "Cannot add negative money.");
      throw new BadRequestException("You cannot add negative money to an account");
    }
    Account account = accountRepository.findByName(name).orElseThrow(EntityMissingException::new);
    account.setBalance(account.getBalance() + request.getBalance());

    Transaction transaction = new Transaction();
    transaction.setCashAmount(request.getBalance());
    transaction.setType(Enums.TransactionType.ADD_FUNDS);
    transaction.setStatus(Enums.TransactionStatus.FILLED);
    transactionRepository.save(transaction);

    if (this.debug)
      loggerService.createAccountTransactionLog(
          name, request.getTransactionId(), "add", request.getBalance());

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

  public Account refundStockFromTransaction(Transaction transaction) {
    Account account = this.getByName(transaction.getUserName());
    Map<String, Long> stocks = account.getPortfolio();
    long newStockAmount = stocks.get(transaction.getStockCode()) + transaction.getStockAmount();
    stocks.put(transaction.getStockCode(), newStockAmount);
    account.setPortfolio(stocks);
    return accountRepository.save(account);
  }

  public Summary generateSummary(String username) throws EntityMissingException {
    Account summaryAccount = getByName(username);

    List<Enums.TransactionStatus> status = new ArrayList<>();
    status.add(Enums.TransactionStatus.COMMITTED);
    // Only gets 1 page of transactions but whatever... TODO...
    List<PendingTransaction> pendingTransactions =
        pendingTransactionRepository.findByUserNameOrderByCreatedDate(username);
    List<Transaction> closedTransactions =
        transactionRepository.findByUserNameAndStatusNotInOrderByCreatedDate(username, status);
    List<Transaction> openTriggers =
        transactionRepository.findByUserNameAndStatusInOrderByCreatedDate(username, status);

    Summary newSummary = new Summary(username, summaryAccount);
    newSummary.setPendingTransactions(pendingTransactions);
    newSummary.setOpenTriggers(openTriggers);
    newSummary.setClosedTransactions(closedTransactions);
    return newSummary;
  }
}
