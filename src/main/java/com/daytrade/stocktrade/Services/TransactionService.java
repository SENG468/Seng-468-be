package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Quote;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

  public Quote getQuote(String userId, String stockSymbol, String transId) {
    return quoteService.quote(userId, stockSymbol, transId);
  }

  public Transaction createSimpleBuyTransaction(Transaction transaction) {
    transaction.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
    double quote =
        getQuote(
                transaction.getUserName(),
                transaction.getStockCode(),
                transaction.getTransactionId())
            .getUnitPrice();
    Account account = accountService.getByName(transaction.getUserName());

    if (transaction.getCashAmount() == null) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.BUY, "Simple Buy - Invalid Request");
      throw new BadRequestException("Invalid Request");
    }

    long stockAmount = (long) (transaction.getCashAmount() / quote);

    if (stockAmount < 1 || (account.getBalance() < transaction.getCashAmount())) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.BUY, "Simple Buy - Insufficient Funds");
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
    transaction.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
    double quote =
        getQuote(
                transaction.getUserName(),
                transaction.getStockCode(),
                transaction.getTransactionId())
            .getUnitPrice();
    Account account = accountService.getByName(transaction.getUserName());
    if (transaction.getCashAmount() == null) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.SELL, "Simple Sell - Invalid Request");
      throw new BadRequestException("Invalid Request");
    }

    long stockAmount = (long) (transaction.getCashAmount() / quote);
    if (stockAmount < 1) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.SELL, "Simple sell - Stock price too high");
      throw new BadRequestException("Stock price too high");
    }
    if (account.getPortfolio().get(transaction.getStockCode()) < stockAmount) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.SELL, "Simple sell - Not enough stock");
      throw new BadRequestException("You do not have the stock for this transaction");
    }
    return createSimpleTransaction(transaction, quote, stockAmount);
  }

  // Make sure to change status to committed or filled here
  public Transaction commitSimpleOrder(Transaction transaction) {
    transaction.setStatus(Enums.TransactionStatus.FILLED);
    return transactionRepository.save(transaction);
  }

  public void expireOrders() {
    // Get All transactions created but not confirmed more than a minute ago
    List<Transaction> expiredTransactions =
        transactionRepository.findAllByStatusAndCreatedDateBefore(
            Enums.TransactionStatus.PENDING, Instant.now().minus(1, ChronoUnit.MINUTES));
    for (Transaction transaction : expiredTransactions) {
      // Cancel Simple transactions. No refunds needed
      if (transaction.getType().equals(Enums.TransactionType.BUY)
          || transaction.getType().equals(Enums.TransactionType.SELL)) {
        transaction.setStatus(Enums.TransactionStatus.EXPIRED);
        Enums.CommandType cmdType =
            transaction.getType().equals(Enums.TransactionType.BUY)
                ? Enums.CommandType.CANCEL_BUY
                : Enums.CommandType.CANCEL_SELL;
        loggerService.createTransactionSysEventLog(transaction, cmdType, null);
      } else if (transaction.getType().equals(Enums.TransactionType.BUY_AT)) {
        // Only committed buy limit orders have refunds needed
        // So no refund needed
        transaction.setStatus(Enums.TransactionStatus.EXPIRED);
        loggerService.createTransactionSysEventLog(transaction, Enums.CommandType.CANCEL_BUY, null);
      } else if (transaction.getType().equals(Enums.TransactionType.SELL_AT)) {
        // Sell limits remove stock from account
        // Refund Stock on order cancel
        accountService.refundStockFromTransaction(transaction);
        transaction.setStatus(Enums.TransactionStatus.EXPIRED);
        loggerService.createTransactionSysEventLog(
            transaction, Enums.CommandType.CANCEL_SELL, null);
      }
    }
    transactionRepository.saveAll(expiredTransactions);
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
        loggerService.createAccountTransactionLog(
            transaction.getUserName(),
            transaction.getTransactionId(),
            "remove",
            account.getBalance());
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
      loggerService.createAccountTransactionLog(
          transaction.getUserName(), transaction.getTransactionId(), "add", account.getBalance());
    }
    return accountService.save(account);
  }

  public Transaction cancelTransaction(Transaction transaction) {
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionRepository.save(transaction);
  }

  public Transaction createLimitTransaction(Transaction transaction) {
    transaction.setUserName(SecurityContextHolder.getContext().getAuthentication().getName());
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
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.SET_SELL_AMOUNT, "Not enough stock");
      throw new BadRequestException("You cannot afford this");
    }
    stocks.put(transaction.getStockCode(), heldStock - stockToSell);
    account.setPortfolio(stocks);
    return accountService.save(account);
  }

  private Account removeMoneyForHold(Double cashAmount, Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    if (account.getBalance() - cashAmount < 0) {
      loggerService.createTransactionErrorLog(
          transaction, Enums.CommandType.COMMIT_BUY, "Trigger - Not enough stock");
      throw new BadRequestException("You cannot afford this");
    }
    account.setBalance(account.getBalance() - cashAmount);
    loggerService.createAccountTransactionLog(
        transaction.getUserName(), transaction.getTransactionId(), "remove", account.getBalance());
    return accountService.save(account);
  }

  public Transaction cancelSellLimitTransaction(Transaction transaction) {
    accountService.refundStockFromTransaction(transaction);
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionRepository.save(transaction);
  }

  public Transaction cancelBuyLimitTransaction(Transaction transaction) {
    Account account = accountService.getByName(transaction.getUserName());
    if (transaction.getStatus().equals(Enums.TransactionStatus.COMMITTED)) {
      account.setBalance(account.getBalance() + transaction.getCashAmount());
      loggerService.createAccountTransactionLog(
          transaction.getUserName(), transaction.getTransactionId(), "add", account.getBalance());
      accountService.save(account);
    }
    transaction.setStatus(Enums.TransactionStatus.CANCELED);
    return transactionRepository.save(transaction);
  }

  public Transaction save(Transaction transaction) {
    return transactionRepository.save(transaction);
  }

  public void fillSellLimitOrders() {
    List<Transaction> orders =
        transactionRepository.findAllByStatusAndType(
            Enums.TransactionStatus.COMMITTED, Enums.TransactionType.SELL_AT);
    for (Transaction order : orders) {
      Quote quote = getQuote(order.getUserName(), order.getStockCode(), order.getTransactionId());
      if (quote.getUnitPrice() >= order.getUnitPrice()) {
        order.setStatus(Enums.TransactionStatus.FILLED);
        // Set the unit price to the quote price if its higher
        order.setUnitPrice(quote.getUnitPrice());
        updateAccount(order);
        loggerService.createTransactionSysEventLog(order, Enums.CommandType.COMMIT_SELL, null);
      }
    }
  }

  public void fillBuyLimitOrders() {
    List<Transaction> orders =
        transactionRepository.findAllByStatusAndType(
            Enums.TransactionStatus.COMMITTED, Enums.TransactionType.BUY_AT);
    for (Transaction order : orders) {
      Quote quote = getQuote(order.getUserName(), order.getStockCode(), order.getTransactionId());
      if (quote.getUnitPrice() <= order.getUnitPrice()) {
        order.setStatus(Enums.TransactionStatus.FILLED);

        // This is adding a double save to the account
        // Change me later
        // Might also be removed depending on whether you buy at new quote or always trigger
        if (quote.getUnitPrice() < order.getUnitPrice()) {
          // Set the unit price to the quote price if its lower
          order.setUnitPrice(quote.getUnitPrice());
          refundForLowerBuyPrice(order);
        }
        updateAccount(order);
        loggerService.createTransactionSysEventLog(order, Enums.CommandType.COMMIT_BUY, null);
      }
    }
  }

  private Account refundForLowerBuyPrice(Transaction order) {
    double newBuyPrice = order.getUnitPrice() * order.getStockAmount();
    double refund = order.getCashAmount() - newBuyPrice;
    Account account = accountService.getByName(order.getUserName());
    account.setBalance(account.getBalance() + refund);
    loggerService.createAccountTransactionLog(
        order.getUserName(), order.getTransactionId(), "add", account.getBalance());
    return accountService.save(account);
  }
}
