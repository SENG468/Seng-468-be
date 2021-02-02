package com.daytrade.stocktrade.Config.Security.Filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
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
    UsernamePasswordAuthenticationToken authenticationToken =
        getAuthentication(request, token, response);
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    try {
      chain.doFilter(request, response);
    } catch (Exception ignored) {
    }
  }

  // Validate the JWT
  private UsernamePasswordAuthenticationToken getAuthentication(
      HttpServletRequest request, String token, HttpServletResponse response) throws IOException {
    try {
      String user =
          JWT.require(Algorithm.HMAC512(SecurityConsts.SECRET.getBytes()))
              .build()
              .verify(token.replace(SecurityConsts.AUTH_HEADER_PREFIX, ""))
              .getSubject();
      if (user != null) {
        return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());
      }
    } catch (TokenExpiredException ex) {
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().write("Your token has expired");
      response.getWriter().flush();
      response.getWriter().close();
    }
    return null;
  }
}
