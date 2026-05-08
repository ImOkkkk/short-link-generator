package cn.imokkkk.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cn.imokkkk.domain.Urls;
import cn.imokkkk.exception.CommonException;
import cn.imokkkk.mapper.UrlMapper;
import cn.imokkkk.pojo.Url;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import tk.mybatis.mapper.entity.Config;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.mapperhelper.EntityHelper;

public class UrlServiceImplTest {

  private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
  private final UrlMapper urlMapper = mock(UrlMapper.class);
  private final UrlServiceImpl urlService = new UrlServiceImpl(
      redisTemplate,
      mock(RedissonClient.class),
      new LinkedBlockingQueue<>());

  @Before
  public void setUp() {
    EntityHelper.initEntityNameMap(Url.class, new Config());
  }

  @After
  public void tearDown() {
    Thread.interrupted();
  }

  @Test(expected = CommonException.class, timeout = 200)
  public void genAndSaveShortUrlFailsWhenQueuePollIsInterrupted() {
    ReflectionTestUtils.setField(urlService, "urlQueue", new LinkedBlockingQueue<Url>());

    Thread.currentThread().interrupt();

    urlService.genAndSaveShortUrl("https://example.com");
  }

  @Test(expected = CommonException.class)
  public void genAndSaveShortUrlDoesNotReturnStaleUrlWhenDatabaseUpdateFails() {
    BlockingQueue<Url> queue = new LinkedBlockingQueue<>();
    queue.add(Url.builder().id(1L).surl("abc123").build());
    ReflectionTestUtils.setField(urlService, "urlQueue", queue);
    ReflectionTestUtils.setField(Urls.class, "urlMapper", urlMapper);
    when(urlMapper.updateByExampleSelective(any(Url.class), any(Example.class))).thenReturn(0);

    try {
      urlService.genAndSaveShortUrl("https://example.com");
    } finally {
      verify(redisTemplate, never()).opsForValue();
    }
  }
}
