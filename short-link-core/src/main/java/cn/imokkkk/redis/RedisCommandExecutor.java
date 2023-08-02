package cn.imokkkk.redis;

import cn.imokkkk.redis.RedisCommandContext.Builder;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.OrderComparator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuwy
 * @date 2023-08-01 16:12
 * @since 1.0
 */
public class RedisCommandExecutor implements InvocationHandler {
    private final RedisConnection redisConnection;
    private final ObjectProvider<RedisCommandInterceptor> redisCommandInterceptors;

    private final RedisSerializer<?> keySerializer;

    private final RedisSerializer<?> valueSerializer;

    private List<RedisCommandInterceptor> interceptors;

    public RedisCommandExecutor(
            RedisConnection redisConnection,
            ObjectProvider<RedisCommandInterceptor> redisCommandInterceptors,
            RedisSerializer<?> keySerializer,
            RedisSerializer<?> valueSerializer) {
        this.redisConnection = redisConnection;
        this.redisCommandInterceptors = redisCommandInterceptors;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 构建执行上下文
        RedisCommandContext context = buildContext(method, args);
        // 构建执行链路
        RedisCommandExecutionChain executionChain =
                new DefaultRedisCommandExecutionChain(getInterceptors());
        // 执行
        return executionChain.execute(context);
    }

    private List<RedisCommandInterceptor> getInterceptors() {
        if (interceptors == null) {
            List<RedisCommandInterceptor> interceptorList = new ArrayList<>();
            this.redisCommandInterceptors.stream().forEach(interceptorList::add);
            if (!interceptorList.isEmpty()) {
                interceptorList =
                        interceptorList.stream()
                                .sorted(OrderComparator.INSTANCE)
                                .collect(Collectors.toList());
            }
            this.interceptors = interceptorList;
        }
        return this.interceptors;
    }

    private RedisCommandContext buildContext(Method method, Object[] params) {
        Builder builder = new Builder(this.redisConnection, method);
        return builder.parameters(params)
                .keySerializer(this.keySerializer)
                .valueSerializer(this.valueSerializer)
                .build();
    }
}
