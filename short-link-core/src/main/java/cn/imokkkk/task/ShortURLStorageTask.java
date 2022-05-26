package cn.imokkkk.task;

import cn.hutool.core.collection.CollUtil;
import cn.imokkkk.mapper.UrlMapper;
import cn.imokkkk.pojo.Url;
import com.alibaba.fastjson.JSON;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * @author ImOkkkk
 * @date 2022/4/27 14:50
 * @since 1.0
 */
public class ShortURLStorageTask implements Callable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ShortURLStorageTask.class);

  private SqlSessionFactory sqlSessionFactory;

  private int commitSize;

  private List<String> shortUrls;

  public ShortURLStorageTask(
      SqlSessionFactory sqlSessionFactory, int commitSize, List<String> shortUrls) {
    this.sqlSessionFactory = sqlSessionFactory;
    this.commitSize = commitSize;
    this.shortUrls = shortUrls;
  }

  @Override
  public Object call(){
    doLoader(shortUrls);
    return shortUrls.size();
  }

  private int doLoader(List<String> shortUrls) {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    List<List<String>> shortUrlLists = CollUtil.splitList(shortUrls, commitSize);
    for (List<String> shortUrlList : shortUrlLists) {
      try {
        storage(shortUrlList);
      } catch (SQLException e) {
        LOGGER.error("storage shortURL error info:{}", JSON.toJSONString(shortUrlList).trim());
      }
    }
    stopWatch.stop();
    LOGGER.info(
        "storage shortURL size:[{}], cost time:[{}]ms",
        shortUrls.size(),
        stopWatch.getTotalTimeMillis());
    return shortUrls.size();
  }

  private void storage(List<String> shortUrlList) throws SQLException {
    if (CollUtil.isNotEmpty(shortUrlList)) {
      SqlSession sqlSession = null;
      try {
        sqlSession = this.sqlSessionFactory.openSession();
        sqlSession.getConnection().setAutoCommit(false);
        UrlMapper urlMapper = sqlSession.getMapper(UrlMapper.class);
        shortUrlList.forEach(
            e -> {
              urlMapper.insertOnDuplicateKeyUpdate(Url.builder().surl(e).createTime(new Date()).build());
            });
        sqlSession.getConnection().commit();
      } catch (Exception e) {
        sqlSession.rollback();
        throw e;
      } finally {
        if (sqlSession != null) {
          sqlSession.close();
        }
      }
    }
  }
}
