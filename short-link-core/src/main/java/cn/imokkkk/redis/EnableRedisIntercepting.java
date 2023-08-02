package cn.imokkkk.redis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * @author liuwy
 * @date 2023-08-02 11:24
 * @since 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({
  RedisCommandInterceptorSelector.class,
  RedisTemplateBeanPostProcessor.class
})
public @interface EnableRedisIntercepting {}
