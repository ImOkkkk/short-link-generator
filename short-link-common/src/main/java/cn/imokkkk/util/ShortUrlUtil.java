package cn.imokkkk.util;

import cn.hutool.core.util.RandomUtil;

/**
 * @author ImOkkkk
 * @date 2022/4/26 10:41
 * @since 1.0
 */
public class ShortUrlUtil {
  private static final char[] toBase64URL = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
  };

  public static String generateShortUrl() {
    StringBuilder urlBuilder = new StringBuilder();
    for (int i = 0; i < 6; i++) {
      urlBuilder.append(generateRandomBase64());
    }
    return urlBuilder.toString();
  }

  private static char generateRandomBase64() {
    int random = RandomUtil.randomInt(0, 63);
    return toBase64URL[random];
  }
}
