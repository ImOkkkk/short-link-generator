package cn.imokkkk.aspect;

import cn.imokkkk.annotaion.RateLimiter;
import cn.imokkkk.constant.LimitType;
import cn.imokkkk.exception.CommonException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author ImOkkkk
 * @date 2022/5/25 10:08
 * @since 1.0
 */
@Component
@Aspect
public class RateLimiterAspect {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimiterAspect.class);

  @Autowired private RedisTemplate<Object, Object> redisTemplate;

  @Autowired
  @Qualifier("rateLimiterScript")
  private DefaultRedisScript rateLimiterScript;

  //  @Before("@annotation(rateLimiter)")
  //  public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable {
  //    String key = rateLimiter.key();
  //    //...
  //  }

  @Before("rateLimiterCut()")
  public void doBefore(JoinPoint joinPoint) {
    MethodSignature sign = (MethodSignature) joinPoint.getSignature();
    Method method = sign.getMethod();
    RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
    if (rateLimiter != null) {
      String key = rateLimiter.key();
      int time = rateLimiter.time();
      int count = rateLimiter.count();

      String combineKey = getCombineKey(rateLimiter, joinPoint);
      List<Object> keys = Collections.singletonList(combineKey);
      Long number = (Long) redisTemplate.execute(rateLimiterScript, keys, count, time);
      if (number == null || number.intValue() > count) {
        throw new CommonException(10001, "访问过于频繁，请稍候再试");
      }
      LOGGER.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), key);
    }
  }

  public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
    StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
    if (rateLimiter.limitType() == LimitType.IP) {
      stringBuffer
          .append(
              getIpAddress(
                  ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                      .getRequest()))
          .append("-");
    }
    MethodSignature signature = (MethodSignature) point.getSignature();
    Method method = signature.getMethod();
    Class<?> targetClass = method.getDeclaringClass();
    stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
    return stringBuffer.toString();
  }

  public String getIpAddress(HttpServletRequest request) {
    // 目前则是网关ip
    String ip = request.getHeader("X-Real-IP");
    if (ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip)) {
      return ip;
    }
    ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !"".equals(ip) && !"unknown".equalsIgnoreCase(ip)) {
      int index = ip.indexOf(',');
      if (index != -1) {
        // 只获取第一个值
        return ip.substring(0, index);
      } else {
        return ip;
      }
    } else {
      // 取不到真实ip则返回空，不能返回内网地址。
      return "";
    }
  }

  @Pointcut("@annotation(cn.imokkkk.annotaion.RateLimiter)")
  public void rateLimiterCut() {}
}
