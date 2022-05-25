package cn.imokkkk.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * @author ImOkkkk
 * @date 2022/4/26 10:29
 * @since 1.0
 */
public class BloomFilterUtil {

  /** 预估数据量 */
  private static final int INSERTIONS = 100000000;
  /** 判重错误率 */
  private static final double FPP = 0.00001;

  private BloomFilter<String> bloomFilter =
      BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), INSERTIONS, FPP);

  private BloomFilterUtil() {}

  public static BloomFilterUtil getInstance() {
    return BloomFilterUtilInstance.BLOOM_FILTER_UTIL_INSTANCE;
  }

  public void addElement(String value) {
    bloomFilter.put(value);
  }

  public boolean containsElement(String value) {
    return bloomFilter.mightContain(value);
  }

  public long getElementCounts() {
    return bloomFilter.approximateElementCount();
  }

  public void writeTo(OutputStream out) throws IOException {
    bloomFilter.writeTo(out);
  }

  private static class BloomFilterUtilInstance {
    private static final BloomFilterUtil BLOOM_FILTER_UTIL_INSTANCE = new BloomFilterUtil();
  }
}
