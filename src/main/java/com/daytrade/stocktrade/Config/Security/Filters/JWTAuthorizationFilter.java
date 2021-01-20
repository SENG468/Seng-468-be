package com.daytrade.stocktrade.Config.Security.Filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.daytrade.stocktrade.Config.Security.SecurityConsts;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

  private final String AUTH_HEADER_NAME = "Authorization";

  public JWTAuthorizationFilter(AuthenticationManager authenticationManager) {
    super(authenticationManager);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    String token = request.getHeader(AUTH_HEADER_NAME);

    // If a token is not presented skip validation
    if (token == null || !token.startsWith("Bearer")) {
      chain.doFilter(request, response);
      return;
    }
    UsernamePasswordAuthenticationToken authenticationToken = getAuthentication(request, token);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    chain.doFilter(request, response);
  }

  // Validate the JWT
  private UsernamePasswordAuthenticationToken getAuthentication(
      HttpServletRequest request, String token) {
    String user =
        JWT.require(Algorithm.HMAC512(SecurityConsts.SECRET.getBytes()))
            .build()
            .verify(token.replace(SecurityConsts.AUTH_HEADER_PREFIX, ""))
            .getSubject();
    if (user != null) {
      return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
    }
    return null;
  }
}
