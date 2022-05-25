package cn.imokkkk.pojo;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

/**
 * @author ImOkkkk
 * @date 2022/4/21 21:25
 * @since 1.0
 */
@Data
@Builder
public class Url {

  private Long id;
  private String surl;//短链接
  private String lurl;//长链接
  private Date createTime;//创建时间

}
