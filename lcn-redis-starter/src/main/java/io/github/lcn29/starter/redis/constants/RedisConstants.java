package io.github.lcn29.starter.redis.constants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <pre>
 * Redis 常量定义
 * </pre>
 *
 * @author lcn29
 * @date 2023-05-21 16:27
 */
public class RedisConstants {

    /**
     * Redis 单词之间的分割符
     */
    public final static String REDIS_KEY_DELIMITER = ":";

    /**
     * 默认的编码
     */
    public final static Charset DEFAULT_CHARSETS = StandardCharsets.UTF_8;

    /**
     * int 类型的 -1
     */
    public final static int INT_MINUS_ONE = -1;

    /**
     * long 类型的 -1
     */
    public final static long LONG_MINUS_ONE = -1L;

    /**
     * int 类型的 0
     */
    public final static int INT_ZERO = 0;

    /**
     * long 类型的 0
     */
    public final static long LONG_ZERO = 0L;

    /**
     * int 类型的 1
     */
    public final static int INT_ONE = 1;

    /**
     * long 类型的 1
     */
    public final static long LONG_ONE = 1L;
    
}
