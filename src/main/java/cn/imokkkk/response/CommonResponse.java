package cn.imokkkk.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * @author ImOkkkk
 * @date 2022/4/21 17:12
 * @since 1.0
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

  private static final long serialVersionUID = -4769652794035776943L;

  private boolean success;

  private String msg;

  private T data;

  public CommonResponse() {
  }

  public CommonResponse(boolean success) {
    this.success = success;
  }

  public CommonResponse(boolean success, String msg) {
    this.success = success;
    this.msg = msg;
  }

  public CommonResponse(boolean success, T data) {
    this.success = success;
    this.data = data;
  }

  public static <T> CommonResponse<T> success() {
    return new CommonResponse<>(true);
  }

  public static <T> CommonResponse<T> successWithMsg(String msg) {
    return new CommonResponse<>(true, msg);
  }

  public static <T> CommonResponse<T> successWithData(T data) {
    return new CommonResponse<>(true, data);
  }

  public static <T> CommonResponse<T> fail() {
    return new CommonResponse<>(false);
  }

  public static <T> CommonResponse<T> failWithMsg(String msg) {
    return new CommonResponse<>(false, msg);
  }
}