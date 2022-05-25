package cn.imokkkk.constant;

/**
 * @author ImOkkkk
 * @date 2022/5/25 9:44
 * @since 1.0
 */
public enum LimitType {
  // 针对当前接口的全局性限流，例如该接口可以在 1 分钟内访问 100 次
  GLOBAL,
  // 针对某一个 IP 地址的限流，例如某个 IP 地址可以在 1 分钟内访问 100 次。
  IP
}
