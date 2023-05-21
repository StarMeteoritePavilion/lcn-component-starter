package io.github.lcn29.starter.redis.cache;

import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Redis 缓存过期对象
 * 调用方必须保证每个属性都有值, 都不能为空
 * </pre>
 *
 * @author lcn29
 * @date 2023-05-21 16:21
 */
public class RedisExpireCache {

    /**
     * Redis Key
     */
    private String redisKey;

    /**
     * Redis Value
     */
    private Object redisValue;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 过期时间单位
     */
    private TimeUnit expireUnit;

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public Object getRedisValue() {
        return redisValue;
    }

    public void setRedisValue(Object redisValue) {
        this.redisValue = redisValue;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public TimeUnit getExpireUnit() {
        return expireUnit;
    }

    public void setExpireUnit(TimeUnit expireUnit) {
        this.expireUnit = expireUnit;
    }
}

