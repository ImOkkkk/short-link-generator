package cn.imokkkk.annotaion;

import cn.imokkkk.constant.LimitType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ImOkkkk
 * @date 2022/5/25 9:46
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
  // 限流key
  String key() default "RATE_LIMIT:";

  // 限流时间，单位秒
  int time() default 60;

  // 限流次数
  int count() default 100;

  // 限流类型
  LimitType limitType() default LimitType.GLOBAL;
}
