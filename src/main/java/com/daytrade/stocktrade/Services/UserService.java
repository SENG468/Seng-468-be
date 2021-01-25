package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.Exceptions.BadRequestException;
import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Repositories.UserRepository;
import com.mongodb.MongoWriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final AccountService accountService;
  private final BCryptPasswordEncoder passwordEncoder;
  private final LoggerService loggerService;

  @Autowired
  public UserService(
      UserRepository userRepository,
      AccountService accountService,
      LoggerService loggerService,
      BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.accountService = accountService;
    this.passwordEncoder = passwordEncoder;
    this.loggerService = loggerService;
  }

  public User createUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    try {
      User out = userRepository.save(user);
      out.setPassword(null);
      accountService.createNewAccount(user.getUsername());
      loggerService.createAccountTransactionLog(user.getUsername(), null, "signup", 0.0);
      return out;
    } catch (MongoWriteException ex) {
      throw new BadRequestException("A user with this username already exist");
    }
  }

  public User findByUsername(String username) {
    loggerService.createAccountTransactionLog(username, null, "login", 0.0);
    return userRepository.findByUsername(username);
  }
}
