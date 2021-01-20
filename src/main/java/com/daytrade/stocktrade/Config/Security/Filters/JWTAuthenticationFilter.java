package com.daytrade.stocktrade.Config.Security.Filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.daytrade.stocktrade.Config.Security.SecurityConsts;
import com.daytrade.stocktrade.Models.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  private AuthenticationManager authenticationManager;
  private final ObjectMapper objectMapper;

  public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
    this.objectMapper = new ObjectMapper();

    // Set the login URL
    setFilterProcessesUrl("/users/login");
  }

  @Override
  public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
    try {
      User creds = objectMapper.readValue(request.getInputStream(), User.class);
      return authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              creds.getUsername(), creds.getPassword(), new ArrayList<>()));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  protected void successfulAuthentication(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain chain,
      Authentication authResult)
      throws IOException {

    String token =
        JWT.create()
            .withSubject(
                ((org.springframework.security.core.userdetails.User) authResult.getPrincipal())
                    .getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + SecurityConsts.EXPIRY))
            .sign(Algorithm.HMAC512(SecurityConsts.SECRET.getBytes()));

    response.setStatus(HttpStatus.OK.value());

    // Add the token to the body
    response.setHeader("Authorization", SecurityConsts.AUTH_HEADER_PREFIX + token);

    // Add the token to the body
    Map<String, String> authResp = new HashMap<>();
    authResp.put("access_token", SecurityConsts.AUTH_HEADER_PREFIX + token);
    response.getWriter().write(objectMapper.writeValueAsString(authResp));
    response.getWriter().flush();
    response.getWriter().close();
  }
}
