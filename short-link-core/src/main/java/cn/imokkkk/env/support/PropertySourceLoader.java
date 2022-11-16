package cn.imokkkk.env.support;

import java.util.Map;

/**
 * @author ImOkkkk
 * @date 2022/11/16 9:24
 * @since 1.0
 */
public interface PropertySourceLoader {

    /**
     * 加载配置
     * @return
     */
    Map<String, String> load();

}
