package com.daytrade.stocktrade.Config.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityConsts {

  public static final String AUTH_HEADER_PREFIX = "Bearer ";
  public static String SECRET;
  public static long EXPIRY;

  @Autowired
  public void loadConfig(
      @Value("${security.expiry-ms}") long expiry, @Value("${security.secret}") String secret) {
    EXPIRY = expiry;
    SECRET = secret;
  }
}
