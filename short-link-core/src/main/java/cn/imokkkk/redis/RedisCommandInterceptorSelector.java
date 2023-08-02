package cn.imokkkk.redis;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author liuwy
 * @date 2023-08-02 11:25
 * @since 1.0
 */
public class RedisCommandInterceptorSelector implements ImportSelector, BeanClassLoaderAware {
    private ClassLoader classLoader;

    protected Class<?> getSpringFactoriesLoaderFactoryClass() {
        return EnableRedisIntercepting.class;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        List<String> beanNames =
                SpringFactoriesLoader.loadFactoryNames(
                        getSpringFactoriesLoaderFactoryClass(), classLoader);
        return StringUtils.toStringArray(beanNames);
    }
}
