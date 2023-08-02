package cn.imokkkk.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author liuwy
 * @date 2023-08-02 11:39
 * @since 1.0
 */
public class ProxiedRedisConnectionFactory implements RedisConnectionFactory {
    private final RedisConnectionFactory connectionFactory;
    protected final ObjectProvider<RedisCommandInterceptor> interceptors;
    private final RedisSerializer<?> keySerializer;
    private final RedisSerializer<?> valueSerializer;

    public ProxiedRedisConnectionFactory(
            RedisConnectionFactory connectionFactory,
            ObjectProvider<RedisCommandInterceptor> interceptors,
            RedisSerializer<?> keySerializer,
            RedisSerializer<?> valueSerializer) {
        this.connectionFactory = connectionFactory;
        this.interceptors = interceptors;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public RedisConnection getConnection() {
        RedisConnection connection = connectionFactory.getConnection();
        return newRedisConnectionProxy(connection);
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return connectionFactory.getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return connectionFactory.getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return connectionFactory.getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException e) {
        return connectionFactory.translateExceptionIfPossible(e);
    }

    protected RedisConnection newRedisConnectionProxy(RedisConnection realRedisConnection) {
        ClassLoader classLoader = realRedisConnection.getClass().getClassLoader();
        InvocationHandler invocationHandler =
                new RedisCommandExecutor(
                        realRedisConnection, this.interceptors, keySerializer, valueSerializer);
        return (RedisConnection)
                Proxy.newProxyInstance(
                        classLoader, new Class[] {RedisConnection.class}, invocationHandler);
    }
}
