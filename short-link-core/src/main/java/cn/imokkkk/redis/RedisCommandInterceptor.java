package cn.imokkkk.redis;

import org.springframework.core.Ordered;

/**
 * @author liuwy
 * @date 2023-08-01 11:17
 * @since 1.0
 */
public interface RedisCommandInterceptor extends Ordered {

    Object execute(
            RedisCommandContext redisCommandContext,
            RedisCommandExecutionChain redisCommandExecutionChain);

    @Override
    default int getOrder() {
        return 100;
    }
}
