package cn.imokkkk.controller;

import cn.hutool.core.lang.Validator;
import cn.imokkkk.request.UrlRequest;
import cn.imokkkk.response.CommonResponse;
import cn.imokkkk.service.UrlService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ImOkkkk
 * @date 2022/4/21 17:08
 * @since 1.0
 */
@RestController
@RequestMapping("/url")
public class UrlController {

  private static final String RECOGNIZED_ID_CARD_URL =
      "http://imgs-sandbox.intsig.net/icr/recognize_id_card?encoding=utf-8&head_portrait=0&crop_image=0";

  //  private static final UrlValidator urlValidator = new UrlValidator();
  @Autowired private UrlService urlService;

  @GetMapping("/pre")
  public CommonResponse preGenerateShortURL(@RequestParam(value = "count") long count) {
    urlService.preGenerateShortURL(count);
    return CommonResponse.success();
  }

  @PostMapping("/gen")
  public CommonResponse generateShortURL(@RequestBody UrlRequest urlRequest) {
    Validator.validateUrl(
        urlRequest.getOriginalURL(), String.format("URL: [%s]非法", urlRequest.getOriginalURL()));
    return CommonResponse.successWithData(
        urlService.genAndSaveShortUrl(urlRequest.getOriginalURL()));
  }

  @GetMapping("/rec/{shortURL}")
  public void transformURL(
      @PathVariable(value = "shortURL") String shortURL, HttpServletResponse response)
      throws IOException {
    response.sendRedirect(urlService.transformURL(shortURL));
  }
}
