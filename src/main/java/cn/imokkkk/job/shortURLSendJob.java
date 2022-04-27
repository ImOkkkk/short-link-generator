package cn.imokkkk.job;

import com.alibaba.fastjson.JSON;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
    List<String> sendList = new ArrayList<>();
    new Thread(
            () -> {
              while (true) {
                try {
                  int size = sendQueue.drainTo(sendList, 100);
                  if (sendList.size() >= 100
                      || (size == 0 && CollectionUtils.isNotEmpty(sendList))) {
                    kafkaTemplate.send("shortURLTopic", JSON.toJSONString(sendList));
                    sendList.clear();
                  }
                  if (size == 0) {
                    Thread.sleep(1000);
                  }
                } catch (Exception e) {
                  log.error("Send shortURL error!", e);
                }
              }
            })
        .start();
  }
}
