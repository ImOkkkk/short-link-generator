package cn.imokkkk.util;

import cn.imokkkk.constant.StatusEnum;
import org.junit.Test;

/**
 * @author liuwy
 * @date 2023-06-12 11:01
 * @since 1.0
 */
public class EnumCacheTest {

  @Test
  public void testFind() {
    System.out.println(EnumCache.findByName(StatusEnum.class, "SUCCESS", null));
    System.out.println(EnumCache.findByValue(StatusEnum.class, "S", null));
  }
}
