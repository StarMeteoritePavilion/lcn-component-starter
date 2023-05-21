package io.github.lcn29.starter.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.lcn29.starter.redis.constants.RedisConstants;
import io.github.lcn29.starter.redis.utils.RedisUtil;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * <pre>
 * Redis Config
 * 当前的配置类需要再 RedisAutoConfiguration 之前注入, 控制自定义的 bean 起作用
 * </pre>
 *
 * @author lcn29
 * @date 2023-05-21 16:21
 */
@Configuration
@AutoConfigureBefore(RedisAutoConfiguration.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private final static Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    private final RedisProperties redisProperties;

    static {
        logger.info("Lcn RedisConfig Init ...");
    }

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(getStandaloneConfig(), getLettuceClientConfiguration());
    }

    /**
     * 获取 StringRedisSerializer, 用来序列化 redis key
     *
     * @return StringRedisSerializer
     */
    @Bean
    public StringRedisSerializer stringRedisSerializer() {
        return new StringRedisSerializer(RedisConstants.DEFAULT_CHARSETS);
    }

    /**
     * 获取 Jackson2JsonRedisSerializer, 用来序列化 redis Value
     *
     * @return Jackson2JsonRedisSerializer
     */
    @Bean
    public Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(redisObjectMapper());
        return jackson2JsonRedisSerializer;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer stringRedisSerializer,
            Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer
    ) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 指定 key 的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 指定 value 的序列化方式
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            StringRedisSerializer stringRedisSerializer,
            Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer
    ) {

        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);

        // 指定 key 的序列化方式
        stringRedisTemplate.setKeySerializer(stringRedisSerializer);
        stringRedisTemplate.setHashKeySerializer(stringRedisSerializer);

        // 指定 value 的序列化方式
        stringRedisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        stringRedisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        String redisAddress = "redis://" + redisProperties.getHost() + ":" + redisProperties.getPort();
        singleServerConfig.setAddress(redisAddress);
        singleServerConfig.setPassword(redisProperties.getPassword());
        return Redisson.create(config);
    }

    @Bean
    public RedisUtil redisUtil(
            StringRedisSerializer stringRedisSerializer,
            Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer,
            RedisTemplate<String, Object> redisTemplate,
            RedissonClient redissonClient
    ) {
        return new RedisUtil(stringRedisSerializer, jackson2JsonRedisSerializer, redisTemplate, redissonClient);
    }

    /**
     * 获取 Redis 单节点的配置
     *
     * @return Redis 单节点配置
     */
    private RedisStandaloneConfiguration getStandaloneConfig() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setPassword(redisProperties.getPassword());
        config.setDatabase(redisProperties.getDatabase());
        return config;
    }

    /**
     * 获取 lettucePool 的配置
     *
     * @return lettucePool 配置
     */
    private LettuceClientConfiguration getLettuceClientConfiguration() {

        GenericObjectPoolConfig<Object> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        genericObjectPoolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
        genericObjectPoolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
        genericObjectPoolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
        genericObjectPoolConfig.setMaxWait(redisProperties.getLettuce().getPool().getMaxWait());

        if (redisProperties.getLettuce().getPool().getTimeBetweenEvictionRuns() != null) {
            genericObjectPoolConfig.setTimeBetweenEvictionRuns(redisProperties.getLettuce().getPool().getTimeBetweenEvictionRuns());
        }

        return LettucePoolingClientConfiguration.builder().commandTimeout(redisProperties.getTimeout())
                .shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout()).poolConfig(genericObjectPoolConfig).build();
    }

    /**
     * 获取 Jackson2JsonRedisSerializer 需要的 ObjectMapper
     *
     * @return ObjectMapper
     */
    private ObjectMapper redisObjectMapper() {

        ObjectMapper redisObjectMapper = new ObjectMapper();
        // 序列化的范围和修饰符的范围 All 全部情况都序列化
        redisObjectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 序列化的类的配置, EVERYTHING 所有类型, 包含 final 修饰的类
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY);
        // 遇到未知属性不失败
        redisObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 序列化中时间的格式的属性处理
        redisObjectMapper.registerModule(new JavaTimeModule());
        // 属性驼峰命名
        redisObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        return redisObjectMapper;
    }
}
