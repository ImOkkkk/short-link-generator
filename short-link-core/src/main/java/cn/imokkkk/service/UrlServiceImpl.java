package cn.imokkkk.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.http.HttpStatus;
import cn.imokkkk.domain.Urls;
import cn.imokkkk.exception.CommonException;
import cn.imokkkk.pojo.Url;
import cn.imokkkk.util.DistributeLock;
import cn.imokkkk.util.ShortUrlUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

/**
 * URL短链接服务实现
 *
 * @author ImOkkkk
 * @date 2022/4/21 17:29
 * @since 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@DependsOn("urls")
public class UrlServiceImpl implements UrlService {

  private static final int QUEUE_INIT_SIZE = 5000;
  private static final int QUEUE_MIN_SIZE = 1000;
  private static final int BLOOM_FILTER_CAPACITY = 20000000;
  private static final double BLOOM_FILTER_ERROR_RATE = 0.00001;
  private static final String BLOOM_FILTER_KEY = "URL_BLOOM_FILTER";
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder()
      .setNameFormat("定时拉取队列-%d")
      .build();

  private final StringRedisTemplate redisTemplate;
  private final RedissonClient redissonClient;

  @Qualifier("shortURLSendQueue")
  private final BlockingQueue<String> sendQueue;

  private BlockingQueue<Url> urlQueue;
  private final ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(1, THREAD_FACTORY);

  @PostConstruct
  public void init() {
    LongAdder longAdder = new LongAdder();
    urlQueue = new LinkedBlockingQueue<>();

    // 初始化加载数据
    initializeUrlQueue(longAdder);

    // 定时刷新队列
    scheduledService.scheduleAtFixedRate(
        () -> replenishUrlQueue(longAdder),
        10,
        1000,
        TimeUnit.MILLISECONDS);
  }

  private void initializeUrlQueue(LongAdder longAdder) {
    List<Url> initUrls = Urls.listLimit(0, QUEUE_INIT_SIZE);
    if (CollUtil.isNotEmpty(initUrls)) {
      urlQueue.addAll(initUrls);
      longAdder.add(initUrls.size());
    }
  }

  private void replenishUrlQueue(LongAdder longAdder) {
    while (urlQueue.size() <= QUEUE_MIN_SIZE) {
      List<Url> urls = Urls.listLimit(longAdder.longValue(), QUEUE_INIT_SIZE);
      if (CollUtil.isNotEmpty(urls)) {
        urlQueue.addAll(urls);
        longAdder.add(urls.size());
      } else {
        break;
      }
    }
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  @DistributeLock(keyExpression = "#originalURL", scene = "genAndSaveShortUrl")
  public String genAndSaveShortUrl(String originalURL) {
    int maxAttempts = Math.max(urlQueue.size(), 1);
    for (int i = 0; i < maxAttempts; i++) {
      Url url = pollAvailableUrl();
      if (url == null) {
        break;
      }
      if (updateUrlMapping(originalURL, url)) {
        cacheShortUrl(url.getSurl(), originalURL);
        return url.getSurl();
      }
    }
    throw new CommonException(HttpStatus.HTTP_INTERNAL_ERROR, "短链接资源不足，请稍后重试");
  }

  private Url pollAvailableUrl() {
    try {
      return urlQueue.poll(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Poll available URL interrupted: {}", e.getMessage());
      return null;
    }
  }

  private boolean updateUrlMapping(String originalURL, Url url) {
    Example example = new Example(Url.class);
    example.createCriteria()
        .andEqualTo("id", url.getId())
        .andIsNull("lurl");
    return Urls.updateSelectiveByExample(Url.builder().lurl(originalURL).build(), example) > 0;
  }

  private void cacheShortUrl(String surl, String originalURL) {
    redisTemplate.opsForValue().set(surl, originalURL);
  }

  @Override
  public String transformURL(String shortURL) {
    return getOriginalUrlFromCache(shortURL)
        .orElseGet(() -> getOriginalUrlFromDatabase(shortURL));
  }

  private Optional<String> getOriginalUrlFromCache(String shortURL) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(shortURL));
  }

  private String getOriginalUrlFromDatabase(String shortURL) {
    Url existURL = Urls.getBySUrl(shortURL);
    if (existURL != null) {
      cacheShortUrl(shortURL, existURL.getLurl());
      return existURL.getLurl();
    }
    throw new CommonException(HttpStatus.HTTP_NOT_FOUND, "URL映射不存在！");
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void preGenerateShortURL(long count) {
    RBloomFilter<Object> bloomFilter = initializeBloomFilter();

    for (long i = 0; i < count; i++) {
      String shortUrl = generateUniqueShortUrl(bloomFilter);
      addToQueue(shortUrl);
    }
  }

  private RBloomFilter<Object> initializeBloomFilter() {
    RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
    if (!bloomFilter.isExists()) {
      bloomFilter.tryInit(BLOOM_FILTER_CAPACITY, BLOOM_FILTER_ERROR_RATE);
    }
    return bloomFilter;
  }

  private String generateUniqueShortUrl(RBloomFilter<Object> bloomFilter) {
    String shortUrl;
    do {
      shortUrl = ShortUrlUtil.generateShortUrl();
    } while (bloomFilter.contains(shortUrl));

    bloomFilter.add(shortUrl);
    return shortUrl;
  }

  private void addToQueue(String shortUrl) {
    try {
      sendQueue.put(shortUrl);
    } catch (InterruptedException e) {
      log.error("Failed to add short URL [{}] to queue: {}", shortUrl, e.getMessage());
    }
  }
}
