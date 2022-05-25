package cn.imokkkk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author ImOkkkk
 * @date 2022/4/21 16:54
 * @since 1.0
 */
@SpringBootApplication
@MapperScan(basePackages = "cn.imokkkk.mapper")
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
