package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.Account;
import com.daytrade.stocktrade.Models.Enums;
import com.daytrade.stocktrade.Models.Transaction;
import com.daytrade.stocktrade.Services.SecurityService;
import com.daytrade.stocktrade.Services.StockService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

  private final StockService stockService;
  private final SecurityService securityService;

  @Autowired
  public TransactionController(StockService stockService, SecurityService securityService) {
    this.stockService = stockService;
    this.securityService = securityService;
  }

  @GetMapping("/quote/{stockSym}")
  public Map<String, Double> getQuote(@PathVariable("stockSym") String stockSym) {
    String name = SecurityContextHolder.getContext().getAuthentication().getName();
    Map<String, Double> out = new HashMap<>();
    Double quote = stockService.getQuote(name, stockSym);
    // Log here
    out.put(stockSym, quote);
    return out;
  }

  @PostMapping("/order/simple")
  public Transaction createOrder(@Valid @RequestBody Transaction transaction) {
    return transaction.getType().equals(Enums.TransactionType.BUY)
        ? stockService.createBuyTransaction(transaction)
        : stockService.createSellTransaction(transaction);
  }

  @PostMapping("/commit/sell")
  public Transaction commitSellOrder() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    Transaction transaction = stockService.getPendingSellTransactions(userName);
    transaction = stockService.commitSimpleOrder(transaction);
    stockService.updateAccount(transaction);
    return transaction;
  }

  @PostMapping("/commit/buy")
  public Account commitBuyOrder() {
    String userName = SecurityContextHolder.getContext().getAuthentication().getName();
    Transaction transaction = stockService.getPendingBuyTransactions(userName);
    transaction = stockService.commitSimpleOrder(transaction);
    return stockService.updateAccount(transaction);
  }
}
