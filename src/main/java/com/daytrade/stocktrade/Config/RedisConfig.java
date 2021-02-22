package com.daytrade.stocktrade.Config;

import java.util.HashMap;
import java.util.Map;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {

  private @Value("${redis.host}") String redisHost;
  private @Value("${redis.port}") int redisPort;

  @Bean(destroyMethod = "shutdown")
  RedissonClient redisson() {
    Config config = new Config();
    config.useSingleServer().setAddress("redis://" + redisHost + ":" + redisPort);
    return Redisson.create(config);
  }

  @Bean
  CacheManager cacheManager(RedissonClient redissonClient) {
    Map<String, CacheConfig> config = new HashMap<>();
    // create "testMap" spring cache with ttl = 1 minutes and maxIdleTime = 12 minutes
    config.put("quotes", new CacheConfig(60 * 1000, 12 * 60 * 1000));
    return new RedissonSpringCacheManager(redissonClient, config);
  }
}
