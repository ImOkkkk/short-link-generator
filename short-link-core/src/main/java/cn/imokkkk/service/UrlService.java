package cn.imokkkk.service;

/**
 * @author ImOkkkk
 * @date 2022/4/21 17:28
 * @since 1.0
 */
public interface UrlService {

  /**
   * 生成短链接
   *
   * @param originalURL
   * @return
   */
  String genAndSaveShortUrl(String originalURL);

  /**
   * 根据短链接映射真实链接
   *
   * @param shortURL
   * @return
   */
  String transformURL(String shortURL);

  /**
   * 预生成短链接
   *
   * @param count
   */
  void preGenerateShortURL(long count);
}
