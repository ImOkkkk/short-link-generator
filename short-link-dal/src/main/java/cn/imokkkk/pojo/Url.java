package cn.imokkkk.pojo;

import java.util.Date;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;

/**
 * @author ImOkkkk
 * @date 2022/4/21 21:25
 * @since 1.0
 */
@Data
@Builder
@Table(name = "url0")
public class Url {

  @Id
  private Long id;
  private String sid;//uuid
  private String surl;//短链接
  private String lurl;//长链接
  private Date createTime;//创建时间

}
