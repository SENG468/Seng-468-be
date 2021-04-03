package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final AccountService accountService;
  private final BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public UserService(
      UserRepository userRepository,
      AccountService accountService,
      BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.accountService = accountService;
    this.passwordEncoder = passwordEncoder;
  }

  public User createUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    try {
      User out = userRepository.save(user);
      out.setPassword(null);
      accountService.createNewAccount(user.getUsername());
      return out;
    } catch (Exception ex) {
      throw new BadRequestException("A user with this username already exist");
    }
  }
  // Required by spring for auth
  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
