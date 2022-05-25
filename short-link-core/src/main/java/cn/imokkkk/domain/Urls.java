package cn.imokkkk.domain;

import cn.imokkkk.mapper.UrlMapper;
import cn.imokkkk.pojo.Url;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

/**
 * @author ImOkkkk
 * @date 2022/4/21 21:45
 * @since 1.0
 */
@Component
public class Urls implements InitializingBean {
  private static Urls urls = null;

  @Override
  public void afterPropertiesSet() {
    urls = this;
  }

  @Resource
  private UrlMapper urlMapper;

  public static void insertSelective(Url url){
    urls.urlMapper.insertSelective(url);
  }

  public static void updateSelectiveByExample(Url url, Example example){
    urls.urlMapper.updateByExampleSelective(url, example);
  }

  public static List<Url> listLimit(long start, long count){
    return urls.urlMapper.listLimit(start, count);
  }

  public static Url getBySUrl(String SUrl) {
    return urls.urlMapper.selectOne(Url.builder().surl(SUrl).build());
  }
}
