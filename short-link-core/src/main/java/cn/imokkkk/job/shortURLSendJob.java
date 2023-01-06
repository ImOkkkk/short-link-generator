package cn.imokkkk.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * @author ImOkkkk
 * @date 2022/4/27 9:44
 * @since 1.0
 */
@Component
@Slf4j
public class shortURLSendJob {

  @Resource(name = "shortURLSendQueue")
  private BlockingQueue<String> sendQueue;

  @Autowired private KafkaTemplate kafkaTemplate;

  @PostConstruct
  public void init() {
    log.info("启动短链接发送JOB...");
    execute();
  }

  // 分阶段提交：数据积累到 500 条需要立即发送 且 3秒钟内未提交立即发送；
  private void execute() {
    new Thread(
            () -> {
              int curIdx = 0;
              long preFT = System.currentTimeMillis();
              List<String> sendList = new ArrayList<>();
              while (true) {
                try {
                  String url = sendQueue.poll(3, TimeUnit.SECONDS);
                  if (StrUtil.isNotBlank(url)) {
                    sendList.add(url);
                    ++curIdx;
                  }
                  if (curIdx <= 0) {
                    continue;
                  }
                  if (StrUtil.isNotBlank(url)
                      && (curIdx == 500 || System.currentTimeMillis() - preFT > 3000)) {
                    kafkaTemplate.send("shortURLTopic", JSON.toJSONString(sendList));
                    curIdx = 0;
                    sendList.clear();
                    preFT = System.currentTimeMillis();
                  }
                  if (Thread.currentThread().isInterrupted()) {
                    break;
                  }
                } catch (InterruptedException e) {
                  log.error("Send shortURL error!", e);
                  Thread.currentThread().interrupt();
                }
              }
            })
        .start();
  }

  // 批量提交
  private void batchExecute() {
    new Thread(
            () -> {
              while (true) {
                try {
                  List<String> sendList = new ArrayList<>();
                  // 阻塞获取
                  String url = sendQueue.take();
                  while (StrUtil.isNotBlank(url)) {
                    sendList.add(url);
                    // 非阻塞式获取一条任务
                    url = sendQueue.poll();
                  }
                  kafkaTemplate.send("shortURLTopic", JSON.toJSONString(sendList));
                  if (Thread.currentThread().isInterrupted()) {
                    break;
                  }
                } catch (InterruptedException e) {
                  log.error("Send shortURL error!", e);
                  Thread.currentThread().interrupt();
                }
              }
            })
        .start();
  }
}
