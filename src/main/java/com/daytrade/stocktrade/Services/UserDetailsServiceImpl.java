package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.User;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
// Override of built in spring class for custom auth
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserService userService;

  @Autowired
  public UserDetailsServiceImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  // Custom load of users to set user storage collection
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userService.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException(username);
    }
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), Collections.emptyList());
  }
}
