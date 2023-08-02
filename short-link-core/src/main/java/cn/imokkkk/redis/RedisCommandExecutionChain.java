package cn.imokkkk.redis;
/**
 * @author liuwy
 * @date 2023-08-01 11:18
 * @since 1.0
 */
public interface RedisCommandExecutionChain {
    Object execute(RedisCommandContext context);
}
