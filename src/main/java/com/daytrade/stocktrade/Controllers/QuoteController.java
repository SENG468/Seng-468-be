package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Services.QuoteService;

import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuoteController {
    private QuoteService quoteService;

    public QuoteController(QuoteService quoteService){
        this.quoteService = quoteService;
    }

}