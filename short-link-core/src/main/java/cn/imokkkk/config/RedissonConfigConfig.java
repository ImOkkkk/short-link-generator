package cn.imokkkk.config;

import cn.hutool.core.util.StrUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ImOkkkk
 * @date 2022/5/10 14:14
 * @since 1.0
 */
@Configuration
public class RedissonConfigConfig {

  @Value("${redisson.redis.host}")
  private String address;

  @Value("${redisson.redis.password:}")
  private String password;

  @Value("${redisson.redis.database:}")
  private int database;

  @Bean
  public Config redissonConfig() {
    Config config = new Config();
    SingleServerConfig singleServerConfig = config.useSingleServer();
    singleServerConfig.setAddress(address);
    singleServerConfig.setDatabase(database);
    if (StrUtil.isNotBlank(password)) {
      singleServerConfig.setPassword(password);
    }
    return config;
  }

  @Bean
  public RedissonClient redissonClient() {
    return Redisson.create(redissonConfig());
  }
}
