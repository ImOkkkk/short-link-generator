package cn.imokkkk.resilience4j;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.Builder;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.autoconfigure.CircuitBreakerProperties;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigurationProperties.InstanceProperties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.embedded.TomcatWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.web.embedded.tomcat.ConfigurableTomcatWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author liuwy
 * @date 2023-06-29 13:17
 * @since 1.0
 */
@Component
public class CircuitBreakerRefresher
        implements ApplicationListener<EnvironmentChangeEvent>,
                ApplicationContextAware,
                EnvironmentAware,
                InitializingBean {
    @Autowired private CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreakerProperties circuitBreakerProperties;
    private ApplicationContext applicationContext;
    private Environment environment;
    private Binder binder;
    // 监听配置列表
    private static final String CIRCUIT_BREAKER_CONFIG_PREFIX =
            "resilience4j.circuitbreaker.backends.";

    private static final Predicate<String> filterConfigKey =
            configKey -> configKey.startsWith(CIRCUIT_BREAKER_CONFIG_PREFIX);

    private static final Function<String, String> getCircuitBreakerNameFunction =
            configKey -> {
                String replacePrefix = configKey.substring(CIRCUIT_BREAKER_CONFIG_PREFIX.length());
                int firstDotIndex = replacePrefix.indexOf('.');
                if (firstDotIndex == -1) {
                    return "";
                }
                return replacePrefix.substring(0, firstDotIndex);
            };

    /**
     * 配置转换成CircuitBreakerConfig
     *
     * @see TomcatWebServerFactoryCustomizer#customize(ConfigurableTomcatWebServerFactory)
     * @see ServletWebServerFactoryCustomizer#customize(ConfigurableServletWebServerFactory)
     */
    public static Function<InstanceProperties, CircuitBreakerConfig> circuitBreakerConfigFunction =
            properties -> {
                if (properties == null) {
                    return CircuitBreakerConfig.ofDefaults();
                }
                Builder builder = CircuitBreakerConfig.custom();
                PropertyMapper propertyMapper = PropertyMapper.get();
                propertyMapper
                        .from(properties.getWaitDurationInOpenState())
                        .whenNonNull()
                        .to(builder::waitDurationInOpenState);
                propertyMapper
                        .from(properties.getSlowCallDurationThreshold())
                        .whenNonNull()
                        .to(builder::slowCallDurationThreshold);
                propertyMapper
                        .from(properties.getMaxWaitDurationInHalfOpenState())
                        .whenNonNull()
                        .to(builder::maxWaitDurationInHalfOpenState);
                propertyMapper
                        .from(properties.getFailureRateThreshold())
                        .whenNonNull()
                        .to(builder::failureRateThreshold);
                propertyMapper
                        .from(properties.getSlowCallDurationThreshold())
                        .whenNonNull()
                        .to(builder::slowCallDurationThreshold);
                propertyMapper
                        .from(properties.getSlidingWindowType())
                        .whenNonNull()
                        .to(builder::slidingWindowType);
                propertyMapper
                        .from(properties.getSlidingWindowSize())
                        .whenNonNull()
                        .to(builder::slidingWindowSize);
                propertyMapper
                        .from(properties.getMinimumNumberOfCalls())
                        .whenNonNull()
                        .to(builder::minimumNumberOfCalls);
                propertyMapper
                        .from(properties.getPermittedNumberOfCallsInHalfOpenState())
                        .whenNonNull()
                        .to(builder::permittedNumberOfCallsInHalfOpenState);
                propertyMapper
                        .from(properties.getAutomaticTransitionFromOpenToHalfOpenEnabled())
                        .whenNonNull()
                        .to(builder::automaticTransitionFromOpenToHalfOpenEnabled);
                propertyMapper
                        .from(properties.getWritableStackTraceEnabled())
                        .whenNonNull()
                        .to(builder::writableStackTraceEnabled);
                propertyMapper
                        .from(properties.getRecordFailurePredicate())
                        .whenNonNull()
                        .to(
                                predicateClass -> {
                                    try {
                                        builder.recordException(
                                                predicateClass
                                                        .getDeclaredConstructor()
                                                        .newInstance());
                                    } catch (InstantiationException
                                            | IllegalAccessException
                                            | InvocationTargetException
                                            | NoSuchMethodException e) {
                                        throw new RuntimeException(e);
                                    }
                                });
                propertyMapper
                        .from(properties.getRecordExceptions())
                        .whenNonNull()
                        .to(builder::recordExceptions);
                propertyMapper
                        .from(properties.getIgnoreExceptions())
                        .whenNonNull()
                        .to(builder::ignoreExceptions);
                return builder.build();
            };

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        // 事件的来源是应用程序上下文对象，即应用程序本身触发了环境变化事件
        if (applicationContext.equals(event.getSource())) {
            doRefreshCircuitBreaker(keys);
        }
    }

    private void doRefreshCircuitBreaker(Set<String> keys) {
        rebindCircuitBreakerProperties();
        Set<String> effectCircuitBreakerNames =
                keys.stream()
                        .filter(filterConfigKey)
                        .map(getCircuitBreakerNameFunction)
                        .collect(Collectors.toSet());
        if (effectCircuitBreakerNames.isEmpty()) {
            return;
        }
        effectCircuitBreakerNames.forEach(this::refreshCircuitBreak);
    }

    /**
     * 刷新指定名称的circuit-breaker
     *
     * @param name
     */
    protected void refreshCircuitBreak(String name) {
        CircuitBreakerConfig config =
                this.circuitBreakerProperties
                        .findCircuitBreakerProperties(name)
                        .map(circuitBreakerConfigFunction)
                        .orElse(CircuitBreakerConfig.ofDefaults());
        CircuitBreaker existedCircuitBreaker = findExistedCircuitBreaker(name);
        if (existedCircuitBreaker == null) {
            this.circuitBreakerRegistry.circuitBreaker(name, config);
        } else {
            CircuitBreaker circuitBreaker = CircuitBreaker.of(name, config);
            this.circuitBreakerRegistry.replace(name, circuitBreaker);
        }
    }

    protected CircuitBreaker findExistedCircuitBreaker(String name) {
        if (ObjectUtils.isEmpty(name)) {
            throw new NullPointerException();
        }
        return this.circuitBreakerRegistry
                .getAllCircuitBreakers()
                .find(circuitBreaker -> name.equals(circuitBreaker.getName()))
                .get();
    }

    /** 读取配置源，绑定至CircuitBreakerProperties */
    private void rebindCircuitBreakerProperties() {
        BindResult<CircuitBreakerProperties> bindResult =
                binder.bind("resilience4j.circuitbreaker", CircuitBreakerProperties.class);
        this.circuitBreakerProperties = bindResult.get();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.binder = Binder.get(this.environment);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}
