package cn.imokkkk.domain;

import cn.imokkkk.mapper.UrlMapper;
import cn.imokkkk.pojo.Url;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * URL领域模型服务
 *
 * <p>
 *   提供URL相关的领域操作，作为业务逻辑和数据库之间的桥梁。
 * </p>
 *
 * @author ImOkkkk
 * @date 2022/4/21 21:45
 * @since 1.0
 */
@Component
public class Urls {

  private static Urls instance;

  private static UrlMapper urlMapper;

  @Autowired
  private UrlMapper injectedMapper;

  @PostConstruct
  public void init() {
    instance = this;
    urlMapper = this.injectedMapper;
  }

  /**
   * 插入URL对象，选择性插入字段（只插入非空字段）
   *
   * @param url 待插入的URL对象
   */
  public static void insertSelective(Url url) {
    validateUrl(url);
    urlMapper.insertSelective(url);
  }

  /**
   * 根据条件选择性更新URL对象（只更新非空字段）
   *
   * @param url 更新的数据
   * @param example 更新条件
   */
  public static int updateSelectiveByExample(Url url, Example example) {
    validateUrl(url);
    validateExample(example);
    return urlMapper.updateByExampleSelective(url, example);
  }

  /**
   * 分页查询URL列表
   *
   * @param start 起始位置
   * @param count 查询数量
   * @return URL列表，如果查询结果为空则返回空列表
   */
  public static List<Url> listLimit(long start, long count) {
    if (start < 0 || count <= 0) {
      return Collections.emptyList();
    }
    return Optional.ofNullable(urlMapper.listLimit(start, count))
        .orElse(Collections.emptyList());
  }

  /**
   * 根据短链接查询URL对象
   *
   * @param surl 短链接字符串
   * @return URL对象，如果不存在则返回null
   */
  public static Url getBySUrl(String surl) {
    return Optional.ofNullable(surl)
        .filter(s -> !s.trim().isEmpty())
        .map(s -> urlMapper.selectOne(Url.builder().surl(s).build()))
        .orElse(null);
  }

  /**
   * 验证URL对象不为null
   */
  private static void validateUrl(Url url) {
    Optional.ofNullable(url)
        .orElseThrow(() -> new IllegalArgumentException("URL对象不能为空"));
  }

  /**
   * 验证示例条件不为null
   */
  private static void validateExample(Example example) {
    Optional.ofNullable(example)
        .orElseThrow(() -> new IllegalArgumentException("更新条件不能为空"));
  }
}
