package cn.imokkkk.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author ImOkkkk
 * @date 2022/4/22 9:58
 * @since 1.0
 */
@Data
public class UrlRequest {
    @NotBlank(message = "原始链接不能为空")
    private String originalURL;
}
