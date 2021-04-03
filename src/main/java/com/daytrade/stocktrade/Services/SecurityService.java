package com.daytrade.stocktrade.Services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.daytrade.stocktrade.Config.Security.SecurityConsts;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

  public String getUserFromJwt(String token) {
    // Load user from the jwt
    return JWT.require(Algorithm.HMAC512(SecurityConsts.SECRET.getBytes()))
        .build()
        .verify(token.replace(SecurityConsts.AUTH_HEADER_PREFIX, ""))
        .getSubject();
  }
}
