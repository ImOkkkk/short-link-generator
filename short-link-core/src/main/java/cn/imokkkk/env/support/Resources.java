package cn.imokkkk.env.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author ImOkkkk
 * @date 2022/11/16 9:22
 * @since 1.0
 */
public class Resources implements ApplicationListener<ApplicationEvent> {
  private static final DeferredLog LOGGER = new DeferredLog();

  public static void releaseJdbcResource(
      Connection conn, PreparedStatement statement, ResultSet rs) {
    try {
      if (conn != null) {
        conn.close();
      }
      if (rs != null) {
        rs.close();
      }
      if (statement != null) {
        statement.close();
      }
    } catch (Exception e) {
      LOGGER.error("release resource error, message: " + e.getMessage(), e);
    }
  }

  @Override
  public void onApplicationEvent(ApplicationEvent applicationEvent) {
    LOGGER.replayTo(Resources.class);
  }
}
