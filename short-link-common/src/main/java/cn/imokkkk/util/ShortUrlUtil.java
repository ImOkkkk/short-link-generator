package cn.imokkkk.util;

import cn.hutool.core.util.RandomUtil;
import java.security.SecureRandom;

/**
 * 短链接生成工具类
 * <p>
 *   使用 Base64 URL 字符集生成简短的随机字符串，适用于生成短链接标识符
 * </p>
 *
 * @author ImOkkkk
 * @date 2022/4/26 10:41
 * @since 1.0
 */
public final class ShortUrlUtil {

  /**
   * Base64 URL 安全字符集（无填充）
   */
  private static final char[] BASE64_URL_CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
  };

  /**
   * 默认生成短链接长度
   */
  private static final int DEFAULT_LENGTH = 6;

  /**
   * Base64 URL 字符集长度
   */
  private static final int BASE64_URL_LENGTH = BASE64_URL_CHARS.length;

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  /**
   * 工具类构造器私有化，防止实例化
   */
  private ShortUrlUtil() {
    throw new AssertionError("工具类禁止实例化");
  }

  /**
   * 生成6位长度的短链接
   *
   * @return 随机生成的6位短链接字符串
   */
  public static String generateShortUrl() {
    return generateShortUrl(DEFAULT_LENGTH);
  }

  /**
   * 生成指定长度的短链接
   *
   * @param length 指定生成短链接的长度
   * @return 随机生成的指定长度短链接字符串
   * @throws IllegalArgumentException 如果长度小于等于0
   */
  public static String generateShortUrl(int length) {
    if (length <= 0) {
      throw new IllegalArgumentException("短链接长度必须大于0");
    }

    StringBuilder urlBuilder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      urlBuilder.append(generateRandomBase64UrlChar());
    }
    return urlBuilder.toString();
  }

  /**
   * 生成随机的 Base64 URL 安全字符
   *
   * @return 随机选择的 Base64 URL 字符
   */
  private static char generateRandomBase64UrlChar() {
    int randomIndex = SECURE_RANDOM.nextInt(BASE64_URL_LENGTH);
    return BASE64_URL_CHARS[randomIndex];
  }
}
