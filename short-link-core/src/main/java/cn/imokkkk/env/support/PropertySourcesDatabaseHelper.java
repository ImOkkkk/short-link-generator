package cn.imokkkk.env.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author ImOkkkk
 * @date 2022/11/16 9:17
 * @since 1.0
 */
public class PropertySourcesDatabaseHelper implements ApplicationListener<ApplicationEvent> {

  public PropertySourcesDatabaseHelper() {}

  private static final DeferredLog LOGGER = new DeferredLog();

  private static final String QUERY_SQL =
      "select config_key, config_value from app_config where is_halt = 'F'";

  private String url;
  private String username;
  private String password;
  private String driverClassName;

  public PropertySourcesDatabaseHelper(
      String url, String username, String password, String driverClassName) {
    this.url = url;
    this.username = username;
    this.password = password;
    this.driverClassName = driverClassName;
  }

  public Map<String, String> getConfigMapByExecuteSql() {

    Map<String, String> configs = new HashMap<>();
    Connection conn = null;
    PreparedStatement statement = null;
    ResultSet rs = null;

    try {
      Class.forName(
          driverClassName == null || driverClassName.isEmpty()
              ? "com.mysql.jdbc.Driver"
              : driverClassName);
      conn = DriverManager.getConnection(this.url, username, password);

      statement = conn.prepareStatement(QUERY_SQL);
      rs = statement.executeQuery();
      while (rs.next()) {
        String configKey = rs.getString("config_key");
        String configValue = rs.getString("config_value");
        configs.put(configKey, configValue);
      }
    } catch (Exception e) {
      LOGGER.error("error to load database configs, message: " + e.getMessage(), e);
    } finally {
      Resources.releaseJdbcResource(conn, statement, rs);
    }

    return configs;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    LOGGER.replayTo(PropertySourcesDatabaseHelper.class);
  }
}
