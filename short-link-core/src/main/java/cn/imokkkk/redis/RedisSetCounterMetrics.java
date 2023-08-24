package cn.imokkkk.redis;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisStringCommands;

import java.lang.reflect.Method;

/**
 * @author liuwy
 * @date 2023-08-02 11:07
 * @since 1.0
 */

public class RedisSetCounterMetrics extends AbstractRedisMetrics<Counter> {

    // http://localhost:6324/actuator/metrics/COUNT.redis.value.keys.set
    public static final String GENERIC_COUNTER_NAME = "COUNT.redis.value.keys.set";

    //  http://localhost:6324/actuator/metrics/COUNT.redis.value.keys.total-set
    public static final String GLOBAL_COUNTER_NAME = "COUNT.redis.value.keys.total-set";

    @Override
    protected boolean determineMonitoring(RedisCommandContext context) {
        Method method = context.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName();
        /**
         * @see RedisStringCommands#set(byte[], byte[])
         */
        return RedisStringCommands.class.equals(declaringClass) && methodName.startsWith("set");
    }

    @Override
    protected Counter buildMeter(RedisCommandContext context, MeterRegistry meterRegistry) {
        Object key = getKey(context, 0);
        String successTag = context.getException() == null ? "true" : "false";
        return Counter.builder(String.format(GENERIC_COUNTER_NAME))
                .tag("key", key.toString())
                .tag("succeed", successTag)
                .register(meterRegistry);
    }

    protected Counter buildGlobalMeter(RedisCommandContext context, MeterRegistry meterRegistry) {
        Object key = getKey(context, 0);
        boolean isSuccess =
                context.getException() == null && Boolean.TRUE.equals(context.getResult());
        return Counter.builder(GLOBAL_COUNTER_NAME)
                .tag(String.format("%s key", isSuccess ? "success" : " fail"), key.toString())
                .register(meterRegistry);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void afterExecute(Counter meter, RedisCommandContext context) {
        // 当前key + 1
        meter.increment();
        // 全局key + 1
        buildGlobalMeter(context, getMeterRegistry()).increment();
    }

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        super.bindTo(meterRegistry);
    }
}
