package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Quote;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

  // Returns quote on hit, null on miss
  @Cacheable(cacheNames = "quotes", key = "#stockSymbol", unless = "#result == null")
  public Quote getCacheQuote(String stockSymbol) {
    return null;
  }

  // Put on cache miss
  @CachePut(cacheNames = "quotes", key = "#stockSymbol")
  public Quote populateCacheQuote(Quote freshQuote, String stockSymbol) {
    return freshQuote;
  }
}
