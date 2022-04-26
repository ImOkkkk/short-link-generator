package cn.imokkkk.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StopWatch;

/**
 * @author ImOkkkk
 * @date 2022/4/26 10:54
 * @since 1.0
 */
@Slf4j
public class ShortUrlUtilTest {

  @Test
  public void generateShortUrl() {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    BloomFilterUtil bloomFilterUtil = BloomFilterUtil.getInstance();
    for (int i = 0; i < 1000000; i++) {
      String shortUrl = ShortUrlUtil.generateShortUrl();
      if (!bloomFilterUtil.containsElement(shortUrl)){
        bloomFilterUtil.addElement(shortUrl);
      }
    }
    stopWatch.stop();
    log.info("耗时{}", stopWatch.getTotalTimeMillis());
    log.info("元素数{}", bloomFilterUtil.getElementCounts());
  }
}
