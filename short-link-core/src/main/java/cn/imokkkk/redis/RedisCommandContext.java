package cn.imokkkk.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liuwy
 * @date 2023-08-01 10:16
 * @since 1.0
 */
public class RedisCommandContext {
    private static final Logger logger = LoggerFactory.getLogger(RedisCommandContext.class);

    private Object[] parameters;

    private Method method;

    private Object result;

    private Map<String, Object> parametersMap = new HashMap<>();

    private long startTimeNanos = -1;

    private long endTimeNanos = -1;

    private RedisConnection connection;

    private RedisSerializer<?> keySerializer;
    private RedisSerializer<?> valueSerializer;
    private volatile Exception exception;

    public Object[] getParameters() {
        return parameters;
    }

    public void setParameters(Object[] parameter) {
        this.parameters = parameter;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Map<String, Object> getParametersMap() {
        return Collections.unmodifiableMap(this.parametersMap);
    }

    public void setParameter(String key, Object value) {
        this.parametersMap.putIfAbsent(key, value);
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public void start() {
        this.startTimeNanos = System.nanoTime();
    }

    public long getEndTimeNanos() {
        return endTimeNanos;
    }

    public void finish() {
        this.endTimeNanos = System.nanoTime();
    }

    public RedisConnection getConnection() {
        return connection;
    }

    public void setConnection(RedisConnection connection) {
        this.connection = connection;
    }

    public RedisSerializer<?> getKeySerializer() {
        return keySerializer;
    }

    public void setKeySerializer(RedisSerializer<?> keySerializer) {
        this.keySerializer = keySerializer;
    }

    public RedisSerializer<?> getValueSerializer() {
        return valueSerializer;
    }

    public void setValueSerializer(RedisSerializer<?> valueSerializer) {
        this.valueSerializer = valueSerializer;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Object execute() throws Exception {
        Object result = this.method.invoke(connection, parameters);
        this.result = result;
        return result;
    }

    public static class Builder {
        private final RedisCommandContext context = new RedisCommandContext();

        public Builder(RedisConnection connection, Method method) {
            this.context.setConnection(connection);
            this.context.setMethod(method);
        }

        public Builder parameters(Object[] parameters) {
            this.context.setParameters(parameters);
            return this;
        }

        public Builder parametersMap(String key, Object value) {
            this.context.setParameter(key, value);
            return this;
        }

        public Builder keySerializer(RedisSerializer<?> keySerializer) {
            this.context.setKeySerializer(keySerializer);
            return this;
        }

        public Builder valueSerializer(RedisSerializer<?> valueSerializer) {
            this.context.setValueSerializer(valueSerializer);
            return this;
        }

        public RedisCommandContext build() {
            return this.context;
        }
    }
}
