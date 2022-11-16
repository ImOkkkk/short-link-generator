package cn.imokkkk.env;

import cn.imokkkk.env.support.DatabasePropertySourceLoader;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author ImOkkkk
 * @date 2022/11/16 9:27
 * @since 1.0
 */
public class DatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    if (environment.getPropertySources().contains("databasePropertySources")) {
      return;
    }
    // 命令行参数
    boolean commandLineArgs = environment.getPropertySources().contains("commandLineArgs");
    if (commandLineArgs) {
      environment
          .getPropertySources()
          .addBefore("commandLineArgs", loadConfigurationFromDatabase(environment));
    } else {
      if (environment.getProperty("spring.datasource.url") != null) {
        environment.getPropertySources().addFirst(loadConfigurationFromDatabase(environment));
        // 设置激活的Profile
        String activeProfile = environment.getProperty("spring.profiles.active", "prd");
        environment.addActiveProfile(activeProfile);
      }
    }
  }

  private PropertySource loadConfigurationFromDatabase(ConfigurableEnvironment environment) {
    String url = environment.getProperty("spring.datasource.url");
    String username = environment.getProperty("spring.datasource.username");
    String password = environment.getProperty("spring.datasource.password");
    String driverClassName = environment.getProperty("spring.datasource.druid.driver-class-name");
    Map<String, ?> configs =
        new DatabasePropertySourceLoader(url, username, password, driverClassName).load();
    PropertySource propertySource =
        new MapPropertySource("databasePropertySources", (Map<String, Object>) configs);
    return propertySource;
  }
}
