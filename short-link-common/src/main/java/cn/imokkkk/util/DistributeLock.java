package cn.imokkkk.util;

import cn.imokkkk.constant.DistributeLockConstant;

/**
 * @author wyliu
 * @date 2025/7/28 20:53
 * @since 1.0
 */
public @interface DistributeLock {

    /**
     * 锁的场景
     * @return
     */
    public String scene();

    /**
     * 加锁的key，优先取key()，如果没有，则取keyExpression()
     * @return
     */
    public String key() default DistributeLockConstant.NONE_KEY;

    /**
     * SPEL表达式
     * @return
     */
    public String keyExpression() default DistributeLockConstant.NONE_KEY;

    /**
     * 超时时间，毫秒
     * 默认情况下不设置超时时间，会自动续期
     * @return
     */
    public int expireTime() default DistributeLockConstant.DEFAULT_EXPIRE_TIME;

    /**
     * 加速等待时长
     * 默认情况下不设置等待时长，不做等待
     * @return
     */
    public int waitTime() default DistributeLockConstant.DEFAULT_WAIT_TIME;
}
