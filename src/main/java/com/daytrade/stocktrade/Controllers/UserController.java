package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.LoginRequest;
import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Services.UserService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/sign-up")
  public User signUp(@Valid @RequestBody User user) {
    return this.userService.createUser(user);
  }

  // DO NOT REMOVE
  // Method stub for the swagger
  @PostMapping("/login")
  public String login(@RequestBody LoginRequest user) {
    return "";
  }
}
