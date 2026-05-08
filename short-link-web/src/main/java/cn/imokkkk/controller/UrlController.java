package cn.imokkkk.controller;

import cn.hutool.core.lang.Validator;
import cn.imokkkk.annotaion.RateLimiter;
import cn.imokkkk.constant.LimitType;
import cn.imokkkk.request.UrlRequest;
import cn.imokkkk.response.CommonResponse;
import cn.imokkkk.service.UrlService;
import cn.imokkkk.util.facade.Facade;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * URL短链接管理控制器
 *
 * @author ImOkkkk
 * @date 2022/4/21 17:08
 * @since 1.0
 */
@RestController
@RequestMapping("/url")
@RequiredArgsConstructor
public class UrlController {

  private static final String URL_VALIDATION_ERROR_MSG = "URL: [%s]非法";
  private final UrlService urlService;

  @GetMapping("/pre")
  public CommonResponse preGenerateShortURL(@RequestParam Long count) {
    urlService.preGenerateShortURL(count);
    return CommonResponse.success();
  }

  @PostMapping("/gen")
  @RateLimiter(time = 1, count = 1000, limitType = LimitType.GLOBAL)
  @Facade
  public CommonResponse generateShortURL(@Valid @RequestBody UrlRequest urlRequest) {
    String originalURL = urlRequest.getOriginalURL();
    Validator.validateUrl(originalURL, String.format(URL_VALIDATION_ERROR_MSG, originalURL));

    String shortUrl = urlService.genAndSaveShortUrl(originalURL);
    return CommonResponse.successWithData(shortUrl);
  }

  @GetMapping("/rec/{shortURL}")
  @RateLimiter(time = 1, count = 1000, limitType = LimitType.GLOBAL)
  public void transformURL(@PathVariable String shortURL, HttpServletResponse response)
      throws IOException {
    String originalUrl = urlService.transformURL(shortURL);
    response.sendRedirect(originalUrl);
  }
}
