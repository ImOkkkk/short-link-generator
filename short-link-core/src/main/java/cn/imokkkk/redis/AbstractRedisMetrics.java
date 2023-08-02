package cn.imokkkk.redis;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.util.Map;

/**
 * @author liuwy
 * @date 2023-08-02 10:05
 * @since 1.0
 */
public abstract class AbstractRedisMetrics<M extends Meter>
        implements RedisCommandInterceptor, MeterBinder {
    private MeterRegistry meterRegistry;

    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public Object execute(RedisCommandContext context, RedisCommandExecutionChain executionChain) {
        // 判断是否拦截
        if (!determineMonitoring(context)) {
            return executionChain.execute(context);
        }
        beforeExecute(buildMeter(context, this.meterRegistry), context);
        Object result = executionChain.execute(context);
        afterExecute(buildMeter(context, this.meterRegistry), context);
        return result;
    }

    /**
     * 判断当前操作是否应该被监控
     *
     * @param context
     * @return
     */
    protected abstract boolean determineMonitoring(RedisCommandContext context);

    /**
     * 根据上下文构建Meter，并注册到MeterRegistry
     *
     * @param context
     * @param meterRegistry
     * @return
     */
    protected abstract M buildMeter(RedisCommandContext context, MeterRegistry meterRegistry);

    protected Object getKey(RedisCommandContext context, int keyParameterIndex) {
        String keyParameter = "redis.execute.key";
        Map<String, Object> parametersMap = context.getParametersMap();
        if (parametersMap.containsKey(keyParameter)) {
            return parametersMap.get(keyParameter);
        }
        Object[] parameters = context.getParameters();
        if (parameters == null || parameters.length == 0) {
            return null;
        }
        if (keyParameterIndex < 0 || keyParameterIndex >= parameters.length) {
            throw new IllegalArgumentException(
              "illegal key parameter index : " + keyParameterIndex);
        }
        byte[] bytes = (byte[]) parameters[keyParameterIndex];
        Object key = context.getKeySerializer().deserialize(bytes);
        context.setParameter(keyParameter, key);
        return key;
    }

    protected void beforeExecute(M meter, RedisCommandContext context) {}

    protected void afterExecute(M meter, RedisCommandContext context) {}

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
}
