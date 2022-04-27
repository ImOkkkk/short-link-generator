package cn.imokkkk.job;

import cn.hutool.core.collection.CollUtil;
import cn.imokkkk.config.KafkaConsumerConfig;
import cn.imokkkk.task.ShortURLStorageTask;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author ImOkkkk
 * @date 2022/4/27 10:23
 * @since 1.0
 */
@Component
@Slf4j
public class ShortURLReceiverJob {

  @Autowired
  @Qualifier("scheduledExecutorService")
  private ScheduledExecutorService scheduledExecutorService;

  @Autowired private KafkaConsumerConfig kafkaConsumerConfig;

  @Resource private SqlSessionFactory sqlSessionFactory;

  @Autowired
  @Qualifier("shortURLSaveThreadPool")
  private ThreadPoolExecutor shortURLSaveThreadPool;

  @Value("${storage.batch.size:200}")
  private int batchSize;

  @PostConstruct
  public void doExecute() {
    Consumer<String, String> consumer =
        kafkaConsumerConfig.getConsumer(Lists.newArrayList("shortURLTopic"));
    scheduledExecutorService.scheduleAtFixedRate(
        () -> {
          try {
            List<String> shortUrls = new ArrayList<>();
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
            if (CollUtil.isNotEmpty(records)) {
              for (ConsumerRecord<String, String> record : records) {
                shortUrls.addAll(JSONArray.parseArray(record.value(), String.class));
              }
            }
            if (!shortUrls.isEmpty()) {
              doTask(shortUrls);
            }
          } catch (Exception e) {
            log.error("shortURLReceiveJob receive error" + e);
          }
        },
        10,
        2000,
        TimeUnit.MILLISECONDS);
  }

  public void doTask(List<String> shortUrls) throws InterruptedException {
    if (CollUtil.isEmpty(shortUrls)) {
      return;
    }
    List<Callable<Integer>> tasks = new ArrayList<>();
    List<List<String>> shortURLLists = Lists.partition(shortUrls, batchSize);
    shortURLLists.forEach(
        shortURLList -> {
          tasks.add(new ShortURLStorageTask(sqlSessionFactory, batchSize, shortURLList));
        });
    shortURLSaveThreadPool.invokeAll(tasks);
  }
}
