package cn.imokkkk.env.support;

import java.util.Map;

/**
 * @author ImOkkkk
 * @date 2022/11/16 9:25
 * @since 1.0
 */
public class DatabasePropertySourceLoader implements PropertySourceLoader {
  private PropertySourcesDatabaseHelper dbHelper;

  public DatabasePropertySourceLoader(
      String url, String username, String password, String driverClassName) {
    this.dbHelper = new PropertySourcesDatabaseHelper(url, username, password, driverClassName);
  }

  @Override
  public Map<String, String> load() {
    return dbHelper.getConfigMapByExecuteSql();
  }
}
