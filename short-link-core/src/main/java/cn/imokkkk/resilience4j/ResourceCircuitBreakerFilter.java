package cn.imokkkk.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import org.apache.catalina.core.ApplicationFilterChain;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * @author liuwy
 * @date 2023-07-05 13:29
 * @since 1.0
 */
public class ResourceCircuitBreakerFilter implements Filter {
    /**
     * org.apache.catalina.core.ApplicationFilterFactory#createFilterChain(javax.servlet.ServletRequest,
     * org.apache.catalina.Wrapper, javax.servlet.Servlet)
     */
    private static final String FILTER_CHAIN_IMPL_CLASS_NAME =
            "org.apache.catalina.core.ApplicationFilterChain";

    private static final Class<?> FILTER_CHAIN_IMPL_CLASS =
            ClassUtils.resolveClassName(FILTER_CHAIN_IMPL_CLASS_NAME, null);

    private CircuitBreakerRegistry circuitBreakerRegistry;

    private Map<String, CircuitBreaker> circuitBreakersCache;

    public ResourceCircuitBreakerFilter(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
//        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom().build();
//        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(circuitBreakerConfig);
        this.circuitBreakersCache = new ConcurrentHashMap<>();
    }

    @Override
    public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String servletName = getServletName(httpServletRequest, filterChain);
        if (servletName == null) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        CircuitBreaker circuitBreaker =
                circuitBreakersCache.computeIfAbsent(
                        servletName, circuitBreakerRegistry::circuitBreaker);
        try {
            circuitBreaker
                    .decorateCheckedRunnable(
                            () -> filterChain.doFilter(servletRequest, servletResponse))
                    .run();
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    private String getServletName(HttpServletRequest httpServletRequest, FilterChain chain)
            throws ServletException {
        String servletName = null;
        if (FILTER_CHAIN_IMPL_CLASS != null) {
            ApplicationFilterChain filterChain = (ApplicationFilterChain) chain;
            try {
                Field field = FILTER_CHAIN_IMPL_CLASS.getDeclaredField("servlet");
                field.setAccessible(true);
                Servlet servlet = (Servlet) field.get(filterChain);
                if (servlet != null) {
                    servletName = servlet.getServletConfig().getServletName();
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        return servletName;
    }
}
