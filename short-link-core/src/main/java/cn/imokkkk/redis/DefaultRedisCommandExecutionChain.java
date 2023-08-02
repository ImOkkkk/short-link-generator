package cn.imokkkk.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liuwy
 * @date 2023-08-01 11:19
 * @since 1.0
 */
public class DefaultRedisCommandExecutionChain implements RedisCommandExecutionChain {
    private static final Logger logger =
            LoggerFactory.getLogger(DefaultRedisCommandExecutionChain.class);

    private List<RedisCommandInterceptor> interceptorList;

    public DefaultRedisCommandExecutionChain(List<RedisCommandInterceptor> interceptorList) {
        this.interceptorList = interceptorList;
    }

    private AtomicInteger interceptorIndex = new AtomicInteger(0);

    @Override
    public Object execute(RedisCommandContext context) {
        if (CollectionUtils.isEmpty(interceptorList)) {
            return executeInternal(context);
        }
        int currIndex = interceptorIndex.get();
        if (currIndex < interceptorList.size()) {
            RedisCommandInterceptor interceptor = interceptorList.get(currIndex);
            if (interceptor == null) {
                throw new NullPointerException();
            }
            interceptorIndex.incrementAndGet();
            return interceptor.execute(context, this);
        }

        return executeInternal(context);
    }

    private Object executeInternal(RedisCommandContext context) {
        try {
            context.start();
            Object result = context.execute();
            context.setResult(result);
            context.finish();
            return result;
        } catch (Exception e) {
            logger.error("redis command execute error: 【{}】 !", e.getMessage(), e);
            context.setResult(null);
            context.finish();
            context.setException(e);
            return null;
        }
    }
}
