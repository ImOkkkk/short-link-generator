package cn.imokkkk.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ImOkkkk
 * @date 2022/4/27 9:17
 * @since 1.0
 */
@Configuration
public class ThreadConfig {
  @Bean
  public BlockingQueue<String> shortURLSendQueue() {
    return new LinkedBlockingQueue<>(20000);
  }

  @Bean(destroyMethod = "shutdown", name = "shortURLSaveThreadPool")
  public ThreadPoolExecutor shortURLSaveThreadPool() {
    ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("短链接入库线程-%d").build();
    return new ThreadPoolExecutor(
        10,
        10,
        60,
        TimeUnit.SECONDS,
        new ArrayBlockingQueue<>(1000),
        tf,
        new ThreadPoolExecutor.CallerRunsPolicy());
  }

  @Bean(destroyMethod = "shutdown", name = "scheduledExecutorService")
  public ScheduledExecutorService scheduledExecutorService() {
    ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("定时扫描线程-%d").build();
    return Executors.newScheduledThreadPool(1, tf);
  }
}
