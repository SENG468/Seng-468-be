package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;
  private final LoggerService loggerService;
  private final QuoteService quoteService;

  public TransactionService(
      TransactionRepository transactionRepository,
      AccountService accountService,
      LoggerService loggerService,
      QuoteService quoteService) {

    this.transactionRepository = transactionRepository;
    this.accountService = accountService;
    this.loggerService = loggerService;
    this.quoteService = quoteService;
  }

  public Double getQuote(String userId, String stockSymbol, String transId) {
    return 20D;
  }

  public Transaction createSimpleBuyTransaction(Transaction transaction) {
    double quote =
        getQuote(transaction.getUserName(), transaction.getStockCode(), transaction.getId());
    Account account = accountService.getByName(transaction.getUserName());

    if (transaction.getCashAmount() == null) {
      loggerService.createErrorEventLog(
          transaction.getUserName(),
          transaction.getId(),
          Enums.CommandType.BUY,
          transaction.getStockCode(),
          null,
          null,
          "Simple Buy - Invalid Request");
      throw new BadRequestException("Invalid Request");
    }

    long stockAmount = (long) (transaction.getCashAmount() / quote);

    if (stockAmount < 1 || (account.getBalance() < transaction.getCashAmount())) {
      loggerService.createErrorEventLog(
          transaction.getUserName(),
          transaction.getId(),
          Enums.CommandType.BUY,
          transaction.getStockCode(),
          null,
          account.getBalance(),
          "Simple Buy - Insufficient Funds");
      throw new BadRequestException("You cannot afford this transaction");
    }
    return createSimpleTransaction(transaction, quote, stockAmount);
  }

  private Transaction createSimpleTransaction(
      Transaction transaction, double quote, long stockAmount) {
    transaction.setUnitPrice(quote);
    transaction.setCashAmount(quote * stockAmount);
    transaction.setStockAmount(stockAmount);
    transaction.setStatus(Enums.TransactionStatus.PENDING);
    return transactionRepository.save(transaction);
  }

  public Transaction createSimpleSellTransaction(Transaction transaction) {
    double quote =
        getQuote(transaction.getUserName(), transaction.getStockCode(), transaction.getId());
    Account account = accountService.getByName(transaction.getUserName());
    if (transaction.getCashAmount() == null) {
      throw new BadRequestException("Invalid Request");
    }

    long stockAmount = (long) (transaction.getCashAmount() / quote);
    if (stockAmount < 1) {
      throw new BadRequestException("Stock price too high");
    }
    if (account.getPortfolio().get(transaction.getStockCode()) < stockAmount) {
      throw new BadRequestException("You do not have the stock for this transaction");
    }
    return createSimpleTransaction(transaction, quote, stockAmount);
  }

  // Make sure to change status to committed or filled here
  public Transaction commitSimpleOrder(Transaction transaction) {
    transaction.setStatus(Enums.TransactionStatus.FILLED);
    return transactionRepository.save(transaction);
  }

  public Transaction getPendingSellTransactions() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusOrderByCreatedDate(
            userName, Enums.TransactionType.SELL, Enums.TransactionStatus.PENDING);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Transaction getPendingBuyTransactions() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> buyTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusOrderByCreatedDate(
            userName, Enums.TransactionType.BUY, Enums.TransactionStatus.PENDING);
    if (buyTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return buyTransactions.get(0);
  }

  public Transaction getPendingLimitBuyTransactions() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> buyTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusOrderByCreatedDate(
            userName, Enums.TransactionType.BUY_AT, Enums.TransactionStatus.PENDING);
    if (buyTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return buyTransactions.get(0);
  }

  public Transaction getPendingLimitSellTransactionsByTicker(String stockTicker) {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusAndStockCodeOrderByCreatedDate(
            userName, Enums.TransactionType.SELL_AT, Enums.TransactionStatus.PENDING, stockTicker);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Transaction getCommittedLimitSellTransactionsByTicker(String stockTicker) {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusAndStockCodeOrderByCreatedDate(
            userName,
            Enums.TransactionType.SELL_AT,
            Enums.TransactionStatus.COMMITTED,
            stockTicker);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Transaction getPendingLimitBuyTransactionsByTicker(String stockTicker) {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusAndStockCodeOrderByCreatedDate(
            userName, Enums.TransactionType.BUY_AT, Enums.TransactionStatus.PENDING, stockTicker);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Transaction getCommittedLimitBuyTransactionsByTicker(String stockTicker) {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusAndStockCodeOrderByCreatedDate(
            userName, Enums.TransactionType.BUY_AT, Enums.TransactionStatus.COMMITTED, stockTicker);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Transaction getPendingLimitSellTransactions() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    List<Transaction> sellTransactions =
        transactionRepository.findByUserNameAndTypeAndStatusOrderByCreatedDate(
            userName, Enums.TransactionType.SELL_AT, Enums.TransactionStatus.PENDING);
    if (sellTransactions.size() < 1) {
      throw new EntityMissingException();
    }
    return sellTransactions.get(0);
  }

  public Account updateAccount(Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    Map<String, Long> stocks = account.getPortfolio();
    // Handel Buy and Buy At orders
    if (transaction.getType().equals(Enums.TransactionType.BUY)
        || transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      // Remove Money from account if buy order
      if (!transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
        account.setBalance(
            account.getBalance() - transaction.getUnitPrice() * transaction.getStockAmount());
      }

      // Update portfolio with new stock counts
      long stockAmount;
      if (stocks.containsKey(transaction.getStockCode())) {
        stockAmount = stocks.get(transaction.getStockCode()) + transaction.getStockAmount();
      } else {
        stockAmount = transaction.getStockAmount();
      }
      stocks.put(transaction.getStockCode(), stockAmount);
      account.setPortfolio(stocks);
      // Handle Sell and Sell At orders
    } else if (transaction.getType().equals(Enums.TransactionType.SELL)
        || transaction.getType().equals(Enums.TransactionType.SELL_AT)) {
      if (!transaction.getType().equals(Enums.TransactionType.SELL_AT)) {
        long stockAmount = stocks.get(transaction.getStockCode()) - transaction.getStockAmount();
        stocks.put(transaction.getStockCode(), stockAmount);
        account.setPortfolio(stocks);
      }
      double newMoney =
          account.getBalance() + transaction.getUnitPrice() * transaction.getStockAmount();
      account.setBalance(newMoney);
    }
    return accountService.save(account);
  }

  public Transaction cancelTransaction(Transaction transaction) {
    return transactionRepository.save(transaction);
  }

  public Transaction createLimitTransaction(Transaction transaction) {
    transaction.setStatus(Enums.TransactionStatus.PENDING);
    if (transaction.getType().equals(Enums.TransactionType.SELL_AT)) {
      // Remove the stock from the portfolio while the order is active
      removeStockForHold(transaction.getStockAmount(), transaction);
    }
    return transactionRepository.save(transaction);
  }

  public Transaction triggerLimitTransaction(
      Transaction savedTransaction, Transaction newTransaction) {
    savedTransaction.setUnitPrice(newTransaction.getUnitPrice());
    savedTransaction.setCashAmount(
        savedTransaction.getUnitPrice() * savedTransaction.getStockAmount());
    savedTransaction.setStatus(Enums.TransactionStatus.COMMITTED);
    if (savedTransaction.getType().equals(Enums.TransactionType.BUY_AT)) {
      // Remove the money from the account while the order is committed
      removeMoneyForHold(savedTransaction.getCashAmount(), savedTransaction);
    }

    return transactionRepository.save(savedTransaction);
  }

  private Account removeStockForHold(Long stockToSell, Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    Map<String, Long> stocks = account.getPortfolio();
    Long heldStock = stocks.getOrDefault(transaction.getStockCode(), null);
    if (heldStock == null || heldStock - stockToSell < 0) {
      throw new BadRequestException("You cannot afford this");
    }
    stocks.put(transaction.getStockCode(), heldStock - stockToSell);
    account.setPortfolio(stocks);
    return accountService.save(account);
  }

  private Account removeMoneyForHold(Double cashAmount, Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    if (account.getBalance() - cashAmount < 0) {
      throw new BadRequestException("You cannot afford this");
    }
    account.setBalance(account.getBalance() - cashAmount);
    return accountService.save(account);
  }

  public Transaction cancelSellLimitTransaction(Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    Map<String, Long> stocks = account.getPortfolio();
    long newStockAmount = stocks.get(transaction.getStockCode()) + transaction.getStockAmount();
    stocks.put(transaction.getStockCode(), newStockAmount);
    account.setPortfolio(stocks);
    accountService.save(account);
    return transactionRepository.save(transaction);
  }

  public Transaction cancelBuyLimitTransaction(Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    if (transaction.getStatus().equals(Enums.TransactionStatus.COMMITTED)) {
      account.setBalance(account.getBalance() + transaction.getCashAmount());
      accountService.save(account);
    }
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionRepository.save(transaction);
  }
}
