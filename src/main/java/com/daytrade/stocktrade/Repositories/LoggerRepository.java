package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Logger;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggerRepository extends MongoRepository<Logger, String> {
  Page<Logger> findAll(Pageable pageable);

  Optional<Page<Logger>> findByUserName(String username, Pageable pageable);
}
