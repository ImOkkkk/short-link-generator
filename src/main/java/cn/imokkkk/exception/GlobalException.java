package cn.imokkkk.exception;

import cn.imokkkk.response.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author ImOkkkk
 * @date 2022/5/25 10:56
 * @since 1.0
 */
@RestControllerAdvice
public class GlobalException {
  @ExceptionHandler(Exception.class)
  public CommonResponse exception(Exception e) {
    if (e instanceof CommonException){
      CommonException commonException = (CommonException) e;
      return CommonResponse.failWithCodeMsg(commonException.errorCode, commonException.getErrorMsg());
    }
    return CommonResponse.failWithMsg(e.getMessage());
  }
}
