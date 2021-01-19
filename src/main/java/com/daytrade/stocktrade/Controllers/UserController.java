package com.daytrade.stocktrade.Controllers;

import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
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
}
