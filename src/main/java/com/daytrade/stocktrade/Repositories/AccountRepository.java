package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Account;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

  Optional<Account> findByName(String name);
}
