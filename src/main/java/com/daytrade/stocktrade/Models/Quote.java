package com.daytrade.stocktrade.Models;

import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Quote {
  public Quote(
      String id,
      String transactionNumber,
      String stockSymbol,
      Double unitPrice,
      Instant timestamp,
      String cryptokey) {
    this.id = id;
    this.transactionNumber = transactionNumber;
    this.stockSymbol = stockSymbol;
    this.unitPrice = unitPrice;
    this.timestamp = timestamp;
    this.cryptoKey = cryptokey;
  }

  private String id;

  private String transactionNumber;

  private String stockSymbol;

  private Double unitPrice;

  private Instant timestamp;

  private String cryptoKey;
}
