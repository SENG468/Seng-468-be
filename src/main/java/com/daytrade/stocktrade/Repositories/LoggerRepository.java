package com.daytrade.stocktrade.Repositories;

import com.daytrade.stocktrade.Models.Logger;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggerRepository extends MongoRepository<Logger, String> {
  List<Logger> findAllByUserName(String username);

  Optional<Page<Logger>> findByUserName(String username, Pageable pageable);
}
