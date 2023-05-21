package io.github.lcn29.starter.redis.utils;

import io.github.lcn29.starter.redis.cache.RedisExpireCache;
import io.github.lcn29.starter.redis.constants.RedisConstants;
import io.github.lcn29.starter.redis.key.RedisKeyDesc;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Redis 工具类
 * </pre>
 *
 * @author lcn29
 * @date 2023-05-21 16:29
 */
public class RedisUtil {

    private final static Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private static StringRedisSerializer STATIC_STRING_REDIS_SERIALIZER;
    private static Jackson2JsonRedisSerializer<Object> STATIC_JACKSON_TO_JSON_REDIS_SERIALIZER;
    private static RedisTemplate<String, Object> STATIC_REDIS_TEMPLATE;
    private static RedissonClient STATIC_REDISSON_CLIENT;

    private final StringRedisSerializer stringRedisSerializer;
    private final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;

    public RedisUtil(StringRedisSerializer stringRedisSerializer,
                     Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer,
                     RedisTemplate<String, Object> redisTemplate,
                     RedissonClient redissonClient) {
        this.stringRedisSerializer = stringRedisSerializer;
        this.jackson2JsonRedisSerializer = jackson2JsonRedisSerializer;
        this.redisTemplate = redisTemplate;
        this.redissonClient = redissonClient;
    }

    /**
     * RedisKey 格式: easicare:用户自定义的格式
     *
     * @param mask 自定义的 redis key 区别标识
     * @return redisKey
     */
    public static String redisKey(RedisKeyDesc redisKeyDesc, String mask) {
        // 拼接调用方需要的格式
        return String.format(redisKeyDesc.desc(), mask);
    }

    /**
     * RedisKey 格式: easicare:用户自定义的格式
     *
     * @param mask 自定义的 redis key 区别标识, 多个
     * @return redisKey
     */
    public static String redisKey(RedisKeyDesc redisKeyDesc, String... mask) {
        // 拼接调用方需要的格式
        return String.format(redisKeyDesc.desc(), mask);
    }

    /**
     * 随机过期时间
     *
     * @param baseTime        基础的时间
     * @param randomTimeRange 随机的时间范围
     * @return 需要的随机时间
     */
    public static long randomExpireTime(Long baseTime, long randomTimeRange) {
        return baseTime + ThreadLocalRandom.current().nextLong(randomTimeRange);
    }

    /**
     * 尝试获取 Redis 锁, 不带超时时间
     * 获取不到锁时会被阻塞
     *
     * @param redisLockKey 锁的 key
     * @return true: 上锁成功, false: 上锁失败
     */
    public static boolean tryLock(String redisLockKey) {
        return tryLock(redisLockKey, null, null);
    }

    /**
     * 尝试获取 Redis 锁, 带超时时间, 单位: 毫秒
     * 获取不到锁时会被阻塞
     *
     * @param redisLockKey 锁的 key
     * @param lockTime     锁的时间, 单位: 毫秒
     * @return true: 上锁成功, false: 上锁失败
     */
    public static boolean tryLock(String redisLockKey, Long lockTime) {
        return tryLock(redisLockKey, lockTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 尝试获取 Redis 锁, 自定义超时时间, 锁的时间和单位, 有一个为空, 会转换为无超时时间的 Redis 锁
     * 获取不到锁时会被阻塞
     *
     * @param redisLockKey 锁的 key
     * @param lockTime     锁的时间
     * @param lockTimeUnit 锁的时间单位
     * @return true: 上锁成功, false: 上锁失败
     */
    public static boolean tryLock(String redisLockKey, Long lockTime, TimeUnit lockTimeUnit) {

        try {
            RLock redisLock = STATIC_REDISSON_CLIENT.getLock(redisLockKey);
            if (lockTime != null && lockTimeUnit != null) {
                redisLock.lock(lockTime, lockTimeUnit);
            } else {
                redisLock.lock();
            }
            return true;
        } catch (Exception ex) {
            logger.info("Get Redis's Lock fail, message:{}", ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * 释放 Redis 锁
     *
     * @param redisLockKey 锁的 key
     * @return true: 是否锁成功, false: 是否锁失败
     */
    public static boolean tryUnLock(String redisLockKey) {
        try {
            RLock redisLock = STATIC_REDISSON_CLIENT.getLock(redisLockKey);
            redisLock.unlock();
            return true;
        } catch (Exception ex) {
            logger.info("Release Redis's Lock fail, message:{}", ex.getMessage(), ex);
        }
        return false;
    }

    /**
     * 给 Redis Key 设置超时时间
     *
     * @param redisKey       Redis Key
     * @param expireTime     过期的时间
     * @param expireTimeUnit 过期时间单位
     */
    public static void expire(String redisKey, long expireTime, TimeUnit expireTimeUnit) {
        STATIC_REDIS_TEMPLATE.expire(redisKey, expireTime, expireTimeUnit);
    }

    /**
     * 通过 Redis Key 删除
     *
     * @param redisKey RedisKey
     */
    public static void delete(String redisKey) {
        STATIC_REDIS_TEMPLATE.delete(redisKey);
    }

    /**
     * 通过 Redis Key 批量删除
     *
     * @param redisKeys RedisKey 列表
     */
    public static void batchDelete(List<String> redisKeys) {
        STATIC_REDIS_TEMPLATE.delete(redisKeys);
    }

    /**
     * 获取 Redis 缓存
     *
     * @param redisKey RedisKey
     * @return RedisValue
     */
    public static <T> T get(String redisKey) {
        Object redisValue = STATIC_REDIS_TEMPLATE.opsForValue().get(redisKey);
        return objectCast(redisValue);
    }

    /**
     * 批量获取缓存
     *
     * @param redisKeyList RedisKey 列表
     * @return RedisValue 列表
     */
    public static <T> List<T> multiGet(List<String> redisKeyList) {

        if (redisKeyList.isEmpty()) {
            return new ArrayList<>();
        }


        List<Object> redisValueList = STATIC_REDIS_TEMPLATE.opsForValue().multiGet(redisKeyList);
        if (redisValueList == null || redisValueList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> returnValue = new ArrayList<>(redisValueList.size());
        redisValueList.stream().filter(Objects::nonNull).forEach(item -> returnValue.add(objectCast(item)));
        return returnValue;
    }

    /**
     * 设置 Redis 缓存
     *
     * @param redisKey   RedisKey
     * @param redisValue RedisValue
     */
    public static void set(String redisKey, Object redisValue) {
        STATIC_REDIS_TEMPLATE.opsForValue().set(redisKey, redisValue);
    }

    /**
     * 批量设置 Redis 缓存
     *
     * @param redisMap 批量设置的缓存 Map, key 为 RedisKey, value: 需要存储的数据
     */
    public static void multiSet(Map<String, Object> redisMap) {
        STATIC_REDIS_TEMPLATE.opsForValue().multiSet(redisMap);
    }

    /**
     * 设置带超时时间的 Redis 缓存
     *
     * @param redisKey       RedisKey
     * @param redisValue     RedisValue
     * @param expireTime     过期时间
     * @param expireTimeUnit 过期时间单位
     */
    public static void setWithExpire(String redisKey, Object redisValue, long expireTime, TimeUnit expireTimeUnit) {
        STATIC_REDIS_TEMPLATE.opsForValue().set(redisKey, redisValue, expireTime, expireTimeUnit);
    }

    /**
     * 批量设置 Redis 缓存, 带超时时间
     *
     * @param redisMap       批量设置的缓存 Map, key 为 RedisKey, value: 需要存储的数据
     * @param expireTime     过期时间
     * @param expireTimeUnit 过期时间单位
     */
    public static void multiSetWithExpire(Map<String, Object> redisMap, long expireTime, TimeUnit expireTimeUnit) {

        STATIC_REDIS_TEMPLATE.executePipelined((RedisCallback<Object>) connection -> {

            for (Map.Entry<String, Object> entry : redisMap.entrySet()) {

                byte[] keySerializeByte = STATIC_STRING_REDIS_SERIALIZER.serialize(entry.getKey());
                byte[] valueSerializeByte = STATIC_JACKSON_TO_JSON_REDIS_SERIALIZER.serialize(entry.getValue());
                if (keySerializeByte == null || valueSerializeByte == null) {
                    continue;
                }
                connection.set(keySerializeByte,
                        valueSerializeByte,
                        Expiration.from(expireTime, expireTimeUnit),
                        RedisStringCommands.SetOption.UPSERT);
            }
            // 这里需要返回 null
            // redisTemplate 内部会根据 RedisCallback 的返回值，判断方法的直接结果
            // 有返回值表示执行异常了, 所以方法正常不用返回值就行了
            // 但是在 Kotlin, for/forEach 会返回 Unit, 同时最后一个返回值会被当做方法返回值
            return null;
        });
    }

    /**
     * 批量设置 Redis 缓存, 带超时时间, 同时期望每个缓存对象的过期时间不一样
     *
     * @param redisExpireCaches 需要设置缓存的对象
     */
    public static void multiSetWithExpire(List<RedisExpireCache> redisExpireCaches) {
        STATIC_REDIS_TEMPLATE.executePipelined((RedisCallback<Object>) connection -> {

            for (RedisExpireCache redisExpireCache : redisExpireCaches) {

                byte[] keySerializeByte = STATIC_STRING_REDIS_SERIALIZER.serialize(redisExpireCache.getRedisKey());
                byte[] valueSerializeByte = STATIC_JACKSON_TO_JSON_REDIS_SERIALIZER.serialize(redisExpireCache.getRedisValue());

                if (keySerializeByte == null || valueSerializeByte == null) {
                    continue;
                }
                connection.set(keySerializeByte,
                        valueSerializeByte,
                        Expiration.from(redisExpireCache.getExpireTime(), redisExpireCache.getExpireUnit()),
                        RedisStringCommands.SetOption.UPSERT
                );
            }
            return null;
        });
    }

    /**
     * 向 List 的右侧添加一个元素, 如果 list 不存在会进行创建
     *
     * @param redisKey   Redis Key
     * @param redisValue 添加的数据
     */
    public static void listAdd(String redisKey, Object redisValue) {
        STATIC_REDIS_TEMPLATE.opsForList().rightPush(redisKey, redisValue);
    }

    /**
     * 向 List 的右侧批量添加元素, 如果 list 不存在会进行创建
     *
     * @param redisKey    Redis Key
     * @param redisValues 添加的数据列表
     */
    public static void listAddAll(String redisKey, List<Object> redisValues) {

        // key 不能为空
        byte[] keySerializeByte = STATIC_STRING_REDIS_SERIALIZER.serialize(redisKey);
        if (keySerializeByte == null) {
            return;
        }

        // redisTemplate 提供的  opsForList 的 rightPushAll 有些问题, 修改为通道批量设置
        STATIC_REDIS_TEMPLATE.executePipelined((RedisCallback<Object>) connection -> {
            for (Object redisValue : redisValues) {
                byte[] valueSerializeByte = STATIC_JACKSON_TO_JSON_REDIS_SERIALIZER.serialize(redisValue);
                connection.rPush(keySerializeByte, valueSerializeByte);
            }
            return null;
        });
    }

    /**
     * 获取 List 中所有的数据
     *
     * @param redisKey Redis Key
     * @return 返回的数据
     */
    public static <T> List<T> listAllGet(String redisKey) {

        List<Object> redisValueList =
                STATIC_REDIS_TEMPLATE.opsForList().range(redisKey, RedisConstants.LONG_ZERO, RedisConstants.LONG_MINUS_ONE);

        if (redisValueList == null || redisValueList.isEmpty()) {
            return new ArrayList<>();
        }

        List<T> returnValue = new ArrayList<>(redisValueList.size());
        redisValueList.stream().filter(Objects::nonNull).forEach(item -> returnValue.add(objectCast(item)));
        return returnValue;

    }

    /**
     * 清空 List 中的所有数据
     *
     * @param redisKey Redis Key
     */
    public static void listClear(String redisKey) {
        // 实际的效果就是删除这个 key
        STATIC_REDIS_TEMPLATE.opsForList().trim(redisKey, RedisConstants.LONG_ONE, RedisConstants.LONG_ZERO);
    }

    /**
     * 从 list 的左边往右删除多少个符合的值
     *
     * @param redisKey       RedisKey
     * @param redisItemValue 需要删除的值
     * @param count          需要删除多少个
     */
    public static void listRemove(String redisKey, Object redisItemValue, Long count) {
        STATIC_REDIS_TEMPLATE.opsForList().remove(redisKey, count, redisItemValue);
    }

    /**
     * 对象强制转换
     *
     * @param obj 转换的类型
     * @return 转换后的对象
     */
    @SuppressWarnings("unchecked")
    private static <T> T objectCast(Object obj) {
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }

    @PostConstruct
    public void initAttributeName() {
        // 在 RedisUtil 实例化后, 初始静态变量的属性
        STATIC_STRING_REDIS_SERIALIZER = stringRedisSerializer;
        STATIC_JACKSON_TO_JSON_REDIS_SERIALIZER = jackson2JsonRedisSerializer;
        STATIC_REDIS_TEMPLATE = redisTemplate;
        STATIC_REDISSON_CLIENT = redissonClient;
    }
}
