package cn.imokkkk.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpStatus;
import cn.imokkkk.domain.Urls;
import cn.imokkkk.exception.CommonException;
import cn.imokkkk.pojo.Url;
import cn.imokkkk.util.BloomFilterUtil;
import cn.imokkkk.util.ShortUrlUtil;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.weekend.WeekendSqls;

/**
 * @author ImOkkkk
 * @date 2022/4/21 17:29
 * @since 1.0
 */
@Service
@Slf4j
@DependsOn("urls")
public class UrlServiceImpl implements UrlService {

  @Autowired private StringRedisTemplate redisTemplate;

  private BlockingQueue<Url> urlQueue;

  @Resource(name = "shortURLSendQueue")
  private BlockingQueue<String> sendQueue;

  @PostConstruct
  public void init() {
    LongAdder longAdder = new LongAdder();
    urlQueue = new LinkedBlockingQueue<>();
    List<Url> initUrls = Urls.listLimit(0, 5000);
    if (CollUtil.isNotEmpty(initUrls)) {
      urlQueue.addAll(initUrls);
      longAdder.add(initUrls.size());
    }
    new Thread(
            () -> {
              while (true) {
                while (urlQueue.size() <= 1000) {
                  List<Url> urls = Urls.listLimit(longAdder.longValue(), 5000);
                  if (CollUtil.isNotEmpty(urls)) {
                    urlQueue.addAll(urls);
                    longAdder.add(urls.size());
                  }else {
                    try {
                      Thread.sleep(1000);
                    } catch (InterruptedException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            })
        .start();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public String genAndSaveShortUrl(String originalURL) {
    Url url = null;
    try {
      url = urlQueue.poll(1000, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (url != null && url.getLurl() == null) {
      Urls.updateSelectiveByExample(
          Url.builder().lurl(originalURL).build(),
          Example.builder(Url.class)
              .where(WeekendSqls.<Url>custom().andEqualTo(Url::getId, url.getId()))
              .build());
      redisTemplate.opsForValue().set(url.getSurl(), originalURL);
      return url.getSurl();
    } else {
      return genAndSaveShortUrl(originalURL);
    }
  }

  @Override
  public String transformURL(String shortURL) {
    // 查找Redis中是否有缓存
    String originalURL = redisTemplate.opsForValue().get(shortURL);
    if (originalURL != null) {
      return originalURL;
    }
    // Redis没有缓存，从数据库查找
    Url existURL = Urls.getBySUrl(shortURL);
    if (existURL != null) {
      // 添加到Redis
      redisTemplate.opsForValue().set(shortURL, existURL.getLurl());
      return existURL.getLurl();
    }
    throw new CommonException(HttpStatus.HTTP_NOT_FOUND, "URL映射不存在！");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void preGenerateShortURL(long count) {
    BloomFilterUtil bloomFilterUtil = BloomFilterUtil.getInstance();
    for (long i = 0; i < count; i++) {
      String shortUrl = ShortUrlUtil.generateShortUrl();
      if (!bloomFilterUtil.containsElement(shortUrl)) {
        bloomFilterUtil.addElement(shortUrl);
        try {
          sendQueue.put(shortUrl);
        } catch (InterruptedException e) {
          log.error(shortUrl + "_" + e.getMessage());
        }
      }
    }
  }
}
