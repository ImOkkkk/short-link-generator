package cn.imokkkk.config;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author ImOkkkk
 * @date 2022/5/25 9:50
 * @since 1.0
 */
@Configuration
public class RedisConfig {

  @Bean
  public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate redisTemplate = new RedisTemplate();
    redisTemplate.setConnectionFactory(connectionFactory);
    // key的序列化采用StringRedisSerializer
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    // 使用FastJsonRedisSerializer 替换默认序列化(默认采用的是JDK序列化)
    FastJsonRedisSerializer<Object> fastJsonRedisSerializer =
        new FastJsonRedisSerializer<>(Object.class);
    redisTemplate.setValueSerializer(fastJsonRedisSerializer);
    redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
    return redisTemplate;
  }
}
