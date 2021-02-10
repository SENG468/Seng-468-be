package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Summary;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Repositories.AccountRepository;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

  private final AccountRepository accountRepository;
  private final TransactionRepository transactionRepository;
  private final LoggerService loggerService;

  @Autowired
  public AccountService(
      AccountRepository accountRepository,
      LoggerService loggerService,
      TransactionRepository transactionRepository) {
    this.accountRepository = accountRepository;
    this.loggerService = loggerService;
    this.transactionRepository = transactionRepository;
  }

  public Account addFundsToAccount(Account request) throws EntityMissingException {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    if (request.getBalance() < 0) {
      throw new BadRequestException("You cannot add negative money to an account");
    }
    Account account = accountRepository.findByName(name).orElseThrow(EntityMissingException::new);
    account.setBalance(account.getBalance() + request.getBalance());

    Transaction transaction = new Transaction();
    transaction.setCashAmount(request.getBalance());
    transaction.setType(Enums.TransactionType.ADD_FUNDS);
    transaction.setStatus(Enums.TransactionStatus.FILLED);
    transactionRepository.save(transaction);

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
    List<Enums.TransactionType> type = new ArrayList<>();
    status.add(Enums.TransactionStatus.PENDING);
    type.addAll(Arrays.asList(Enums.TransactionType.BUY_AT, Enums.TransactionType.SELL_AT));
    List<Transaction> pendingTriggers =
        transactionRepository.findByUserNameAndStatusInAndTypeInOrderByCreatedDate(
            username, status, type);

    status.remove(Enums.TransactionStatus.PENDING);
    status.addAll(
        Arrays.asList(
            Enums.TransactionStatus.CANCELED,
            Enums.TransactionStatus.COMMITTED,
            Enums.TransactionStatus.FILLED,
            Enums.TransactionStatus.EXPIRED));
    type.addAll(Arrays.asList(Enums.TransactionType.BUY, Enums.TransactionType.SELL));
    List<Transaction> closedTransactions =
        transactionRepository.findByUserNameAndStatusInAndTypeInOrderByCreatedDate(
            username, status, type);

    Summary newSummary = new Summary(username, summaryAccount);
    newSummary.setPendingTriggers(pendingTriggers);
    newSummary.setClosedTransactions(closedTransactions);
    return newSummary;
  }
}
