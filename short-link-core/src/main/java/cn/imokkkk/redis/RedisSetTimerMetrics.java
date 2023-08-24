package cn.imokkkk.redis;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.RedisStringCommands;

import java.lang.reflect.Method;

/**
 * @author liuwy
 * @date 2023-08-02 15:55
 * @since 1.0
 */
public class RedisSetTimerMetrics extends AbstractRedisMetrics<Timer> {
    // http://localhost:6324/actuator/metrics/TIMER.redis.value.keys.set
    private static final String GENERIC_TIMER_NAME = "TIMER.redis.value.keys.set";

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
    public Object execute(RedisCommandContext context, RedisCommandExecutionChain executionChain) {
        if (!determineMonitoring(context)) {
            return executionChain.execute(context);
        }
        Timer timer = buildMeter(context, getMeterRegistry());
        // 记录链路调用
        return timer.record(() -> executionChain.execute(context));
    }

    @Override
    protected Timer buildMeter(RedisCommandContext context, MeterRegistry meterRegistry) {
        Object key = getKey(context, 0);
        return Timer.builder(GENERIC_TIMER_NAME).tag("key", key.toString()).register(meterRegistry);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
