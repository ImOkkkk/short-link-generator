package cn.imokkkk.constant;

import cn.imokkkk.util.EnumCache;

/**
 * @author liuwy
 * @date 2023-06-12 11:02
 * @since 1.0
 */
public enum StatusEnum {
  INIT("I", "初始化"),
  PROCESSING("P", "处理中"),
  SUCCESS("S", "成功"),
  FAIL("F", "失败");

  private String code;
  private String desc;

  StatusEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return code;
  }

  public String getDesc() {
    return desc;
  }

  static {
    // 通过名称构建缓存,通过EnumCache.findByName(StatusEnum.class,"SUCCESS",null);调用能获取枚举
    EnumCache.registerByName(StatusEnum.class, StatusEnum.values());
    // 通过code构建缓存,通过EnumCache.findByValue(StatusEnum.class,"S",null);调用能获取枚举
    EnumCache.registerByValue(StatusEnum.class, StatusEnum.values(), StatusEnum::getCode);
  }
}
