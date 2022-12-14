package cn.imokkkk.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.id.NanoId;
import cn.imokkkk.config.KafkaConsumerConfig;
import cn.imokkkk.mapper.UrlMapper;
import cn.imokkkk.pojo.Url;
import cn.imokkkk.task.ShortURLStorageTask;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author ImOkkkk
 * @date 2022/4/27 10:23
 * @since 1.0
 */
@Component
@Slf4j
public class ShortURLReceiverJob {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShortURLReceiverJob.class);

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

  private EventBus eventBus;

  @PostConstruct
  public void doExecute() {
    Consumer<String, String> consumer =
        kafkaConsumerConfig.getConsumer(Lists.newArrayList("shortURLTopic"));
    scheduledExecutorService.scheduleAtFixedRate(
        () -> {
          List<String> shortUrls = new ArrayList<>();
          ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
          if (CollUtil.isNotEmpty(records)) {
            for (ConsumerRecord<String, String> record : records) {
              shortUrls.addAll(JSONArray.parseArray(record.value(), String.class));
            }
          }
          Thread currentThread = Thread.currentThread();
          if (currentThread.isInterrupted()) {
            return;
          }
          try {
            if (!shortUrls.isEmpty()) {
              //Guava EventBus实现
              if (eventBus == null) {
                eventBus = new AsyncEventBus(shortURLSaveThreadPool);
                this.eventBus.register(new StorageEventListener());
              }
              List<List<String>> shortURLLists = Lists.partition(shortUrls, batchSize);
              shortURLLists.forEach(s -> eventBus.post(s));
              //doTask(shortUrls);
            } else {
              currentThread.sleep(1000);
            }
          } catch (InterruptedException e) {
            log.error("shortURLReceiveJob receive error" + e);
            currentThread.interrupt();
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

  public class StorageEventListener {
    @Subscribe
    public void storage(List<String> shortUrls) {
      if (CollUtil.isNotEmpty(shortUrls)) {
        SqlSession sqlSession = null;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
          sqlSession = sqlSessionFactory.openSession();
          sqlSession.getConnection().setAutoCommit(false);
          UrlMapper urlMapper = sqlSession.getMapper(UrlMapper.class);
          shortUrls.forEach(
              e -> {
                urlMapper.insertOnDuplicateKeyUpdate(
                    Url.builder()
                        .sid(NanoId.randomNanoId())
                        .surl(e)
                        .createTime(new Date())
                        .build());
              });
          sqlSession.getConnection().commit();
          stopWatch.stop();
          LOGGER.info(
              "storage shortURL size:[{}], cost time:[{}]ms",
              shortUrls.size(),
              stopWatch.getTotalTimeMillis());
        } catch (Exception e) {
          log.error("storage shortURL to DB error : {}", e.getMessage(), e);
          sqlSession.rollback();
        } finally {
          if (sqlSession != null) {
            sqlSession.close();
          }
        }
      }
    }
  }
}
