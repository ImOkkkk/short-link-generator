package cn.imokkkk.mapper;

import cn.imokkkk.pojo.Url;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author ImOkkkk
 * @date 2022/4/21 21:23
 * @since 1.0
 */
@Repository
public interface UrlMapper extends Mapper<Url> {

  List<Url> listLimit(@Param("start") long start, @Param("limit") long count);
}
