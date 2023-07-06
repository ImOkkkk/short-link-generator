package cn.imokkkk.resilience4j;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liuwy
 * @date 2023-06-25 15:42
 * @since 1.0
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BulkheadWebFilter
        implements WebFilter, EnvironmentAware, BeanFactoryAware, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(BulkheadWebFilter.class);

    public static final String BULKHEAD_ATTRIBUTE_NAME = Bulkhead.class.getName() + "@WebFlux";
    private ObjectProvider<HandlerMapping> handlerMappingProvider;
    private BeanFactory beanFactory;
    private Environment environment;
    private BulkheadRegistry bulkheadRegistry;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void destroy() throws Exception {
        if (this.bulkheadRegistry != null) {
            this.clearBulkheadRegistry(bulkheadRegistry);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.bulkheadRegistry = this.initializeBulkheadRegistry(this.environment, this.beanFactory);
        this.handlerMappingProvider = this.beanFactory.getBeanProvider(HandlerMapping.class);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    protected void clearBulkheadRegistry(BulkheadRegistry bulkheadRegistry) {
        List<String> bulkheadNames =
                bulkheadRegistry.getAllBulkheads().asJava().stream()
                        .map(Bulkhead::getName)
                        .collect(Collectors.toList());
        for (String bulkheadName : bulkheadNames) {
            bulkheadRegistry.remove(bulkheadName);
        }
    }

    private BulkheadRegistry initializeBulkheadRegistry(
            Environment environment, BeanFactory beanFactory) {
        // TODO 自定义
        return BulkheadRegistry.ofDefaults();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, WebFilterChain webFilterChain) {
        HandlerMethod handlerMethod =
                retrieveHandlerMethod(this.handlerMappingProvider, serverWebExchange);
        if (logger.isDebugEnabled()) {
            logger.debug("try decorate request:{}", handlerMethod.toString());
        }
        Bulkhead bulkhead =
                this.bulkheadRegistry.bulkhead(
                        handlerMethod.toString(),
                        customizeBulkheadConfig(handlerMethod, this.environment, this.beanFactory));

        return bulkhead.executeSupplier(() -> webFilterChain.filter(serverWebExchange));
    }

    private BulkheadConfig customizeBulkheadConfig(
            HandlerMethod handlerMethod, Environment environment, BeanFactory beanFactory) {

        return BulkheadConfig.custom().maxConcurrentCalls(5).build();
        // default implements
        // return BulkheadConfig.ofDefaults();
    }

    private HandlerMethod retrieveHandlerMethod(
            ObjectProvider<HandlerMapping> handlerMappingProvider,
            ServerWebExchange serverWebExchange) {
        return Flux.fromStream(handlerMappingProvider.stream())
                .concatMap(mapping -> mapping.getHandler(serverWebExchange))
                .next()
                .map(HandlerMethod.class::cast)
                .block();
    }
}
