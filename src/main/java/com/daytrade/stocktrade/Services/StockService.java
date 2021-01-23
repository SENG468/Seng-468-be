package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.Exceptions.EntityMissingException;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Repositories.TransactionRepository;
import java.util.Map;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StockService {

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;

  public StockService(TransactionRepository transactionRepository, AccountService accountService) {

    this.transactionRepository = transactionRepository;
    this.accountService = accountService;
  }

  public Double getQuote(String userId, String stockSymbol) {
    return 20D;
  }

  public Transaction createSimpleBuyTransaction(Transaction transaction) {
    double quote = getQuote(transaction.getUserName(), transaction.getStockCode());
    Account account = accountService.getByName(transaction.getUserName());

    if (transaction.getCashAmount() == null) {
      throw new BadRequestException("Invalid Request");
    }

    long stockAmount = (long) (transaction.getCashAmount() / quote);

    if (stockAmount < 1 || (account.getBalance() < transaction.getCashAmount())) {
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
    double quote = getQuote(transaction.getUserName(), transaction.getStockCode());
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
    return transactionRepository
        .findByUserNameAndTypeAndStatus(
            userName, Enums.TransactionType.SELL, Enums.TransactionStatus.PENDING)
        .orElseThrow(EntityMissingException::new);
  }

  public Transaction getPendingBuyTransactions() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    return transactionRepository
        .findByUserNameAndTypeAndStatus(
            userName, Enums.TransactionType.BUY, Enums.TransactionStatus.PENDING)
        .orElseThrow(EntityMissingException::new);
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
    transactionRepository.save(transaction);
  }
}
