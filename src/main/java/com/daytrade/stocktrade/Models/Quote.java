package com.daytrade.stocktrade.Models;

import lombok.Data;

@Data
public class Quote {
    private String id;

    private String transactionNumber;

    private String stockSymbol;

    private Double unitPrice;

    private Long quoteServerTime;

    private String cryptoKey;
}
