package cn.imokkkk.aspect;

import cn.imokkkk.constant.DistributeLockConstant;
import cn.imokkkk.util.DistributeLock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * @author wyliu
 * @date 2025/7/28 21:00
 * @since 1.0
 */
@Aspect
@Component
public class DistributeLockAspect {
    private static final Logger logger = LoggerFactory.getLogger(DistributeLockAspect.class);

    @Autowired private RedissonClient redissonClient;

    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final StandardReflectionParameterNameDiscoverer parameterNameDiscoverer =
            new StandardReflectionParameterNameDiscoverer();

    @Around("@annotation(cn.imokkkk.util.DistributeLock)")
    public Object process(ProceedingJoinPoint joinPoint) throws Exception {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);

        String lockKey = buildLockKey(joinPoint, method, distributeLock);
        RLock rLock = redissonClient.getLock(lockKey);

        boolean locked = acquireLock(rLock, distributeLock, lockKey);
        if (!locked) {
            throw new RuntimeException("lock for key: " + lockKey + " fail");
        }

        try {
            logger.info("lock success for key: {}", lockKey);
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new Exception(e);
        } finally {
            rLock.unlock();
        }
    }

    private String buildLockKey(
            ProceedingJoinPoint joinPoint, Method method, DistributeLock distributeLock) {
        String key = distributeLock.key();

        if (DistributeLockConstant.NONE_KEY.equals(key)) {
            key = resolveKeyFromExpression(joinPoint, method, distributeLock);
        }

        return key + "#" + distributeLock.scene();
    }

    private String resolveKeyFromExpression(
            ProceedingJoinPoint joinPoint, Method method, DistributeLock distributeLock) {
        String keyExpression = distributeLock.keyExpression();
        if (DistributeLockConstant.NONE_KEY.equals(keyExpression)) {
            throw new RuntimeException("not lock key found");
        }

        Expression expression = parser.parseExpression(keyExpression);
        EvaluationContext context = new StandardEvaluationContext();

        Object[] args = joinPoint.getArgs();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);

        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                context.setVariable(parameterNames[i], args[i]);
            }
        }

        return String.valueOf(expression.getValue(context));
    }

    private boolean acquireLock(RLock rLock, DistributeLock distributeLock, String lockKey)
            throws InterruptedException {
        int expireTime = distributeLock.expireTime();
        int waitTime = distributeLock.waitTime();

        if (isDefaultValues(waitTime, expireTime)) {
            logger.info("lock for key: {}", lockKey);
            rLock.lock();
            return true;
        }

        if (isDefaultWaitTime(waitTime)) {
            logger.info("lock for key: {} with expire time: {}", lockKey, expireTime);
            rLock.lock(expireTime, TimeUnit.MILLISECONDS);
            return true;
        }

        if (isDefaultExpireTime(expireTime)) {
            logger.info("lock for key: {} with wait time: {}", lockKey, waitTime);
            return rLock.tryLock(waitTime, TimeUnit.MILLISECONDS);
        }

        logger.info(
                "lock for key: {} with wait time: {} and expire time: {}",
                lockKey,
                waitTime,
                expireTime);
        return rLock.tryLock(waitTime, expireTime, TimeUnit.MILLISECONDS);
    }

    private boolean isDefaultValues(int waitTime, int expireTime) {
        return waitTime == DistributeLockConstant.DEFAULT_WAIT_TIME
                && expireTime == DistributeLockConstant.DEFAULT_EXPIRE_TIME;
    }

    private boolean isDefaultWaitTime(int waitTime) {
        return waitTime == DistributeLockConstant.DEFAULT_WAIT_TIME;
    }

    private boolean isDefaultExpireTime(int expireTime) {
        return expireTime == DistributeLockConstant.DEFAULT_EXPIRE_TIME;
    }
}
