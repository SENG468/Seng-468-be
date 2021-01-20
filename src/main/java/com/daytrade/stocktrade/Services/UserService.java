package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;
  private final BCryptPasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User createUser(User user) {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    User out = userRepository.save(user);
    out.setPassword(null);
    return out;
  }

  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }
}
