package cn.imokkkk;

import cn.imokkkk.util.ShortUrlUtil;
import com.google.common.collect.Sets;
import java.util.HashSet;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;

/**
 * @author ImOkkkk
 * @date 2022/5/26 8:59
 * @since 1.0
 */
public class ShortUrlUtilTest {

  @Test
  public void generateShortUrl() {
    HashSet<@Nullable Object> urls = Sets.newHashSetWithExpectedSize(4000000);
    for (int i = 0; i < 10000000; i++) {
      urls.add(ShortUrlUtil.generateShortUrl());
    }
    System.out.println("size:" + urls.size());
  }
}
