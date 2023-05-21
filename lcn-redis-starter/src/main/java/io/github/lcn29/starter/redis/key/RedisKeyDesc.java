package io.github.lcn29.starter.redis.key;

/**
 * <pre>
 * Redis Key 枚举接口实现类
 * </pre>
 *
 * @author lcn29
 * @date 2023-05-21 16:28
 */
public interface RedisKeyDesc {

    /**
     * 获取用户自定义部分的 Redis Key 的描述
     *
     * @return key 的描述
     */
    String desc();
}
