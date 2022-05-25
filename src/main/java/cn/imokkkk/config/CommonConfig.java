package cn.imokkkk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author ImOkkkk
 * @date 2022/5/25 10:04
 * @since 1.0
 */
@Configuration
public class CommonConfig {

  @Bean("rateLimiterScript")
  public DefaultRedisScript<Long> rateLimiterScript(){
    DefaultRedisScript<Long> rateLimiterScript = new DefaultRedisScript<>();
    rateLimiterScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/rateLimiter.lua")));
    rateLimiterScript.setResultType(Long.class);
    return rateLimiterScript;
  }
}
