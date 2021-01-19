package com.daytrade.stocktrade.Services;

import com.daytrade.stocktrade.Models.User;
import com.daytrade.stocktrade.Repositories.UserRepository;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        User out = userRepository.save(user);
        out.setPassword(null);
        return out;
    }

}
