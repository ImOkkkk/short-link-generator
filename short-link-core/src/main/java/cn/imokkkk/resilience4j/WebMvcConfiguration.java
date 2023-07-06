package cn.imokkkk.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import java.util.Collections;

import javax.servlet.DispatcherType;

/**
 * @author liuwy
 * @date 2023-07-05 13:11
 * @since 1.0
 */
@Configuration
public class WebMvcConfiguration {

    @Bean
    public FilterRegistrationBean<ResourceCircuitBreakerFilter> resourceCircuitBreakerFilter(
            CircuitBreakerRegistry circuitBreakerRegistry) {
        FilterRegistrationBean<ResourceCircuitBreakerFilter> registrationBean =
                new FilterRegistrationBean<>();
        registrationBean.setFilter(new ResourceCircuitBreakerFilter(circuitBreakerRegistry));
        registrationBean.setName("resourceCircuitBreakerFilter");
        registrationBean.setUrlPatterns(Collections.singleton("/*"));
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }
}
