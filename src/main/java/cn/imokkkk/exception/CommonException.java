package cn.imokkkk.exception;

/**
 * @author ImOkkkk
 * @date 2022/4/22 9:33
 * @since 1.0
 */
public class CommonException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  protected int errorCode;
  protected String errorMsg;
  protected Object data;

  public CommonException() {
  }

  public CommonException(int errorCode) {
    this.errorCode = errorCode;
  }

  public CommonException(int errorCode, String errorMsg) {
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
  }

  public CommonException(int errorCode, Throwable cause) {
    super(cause);
    this.errorCode = errorCode;
  }

  public CommonException(String errorMsg, Throwable cause) {
    super(cause);
    this.errorMsg = errorMsg;
    this.errorCode = -1;
  }

  public CommonException(int errorCode, String errorMsg, Object data) {
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
    this.data = data;
  }

  public int getErrorCode() {
    return this.errorCode;
  }

  public String getErrorMsg() {
    return this.errorMsg;
  }

  public Object getData() {
    return this.data;
  }
}

