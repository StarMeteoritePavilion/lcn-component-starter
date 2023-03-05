package io.github.lcn29.mysql.starter;

import io.github.lcn29.mysql.starter.page.MySqlPageInterceptor;
import io.github.lcn29.mysql.starter.sharding.ShardingSphereResourceInitEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * <pre>
 * MySQL 配置类
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 21:50
 */
@EnableConfigurationProperties({LcnMySQLProperty.class})
public class LcnMySQLConfig {

    private final LcnMySQLProperty lcnMySQLProperty;

    public LcnMySQLConfig(LcnMySQLProperty lcnMySQLProperty) {
        this.lcnMySQLProperty = lcnMySQLProperty;
    }

    /**
     * MySQL 分页拦截器
     *
     * @return MySQL 分页拦截器
     */
    @Bean
    public MySqlPageInterceptor mySqlPageInterceptor() {
        return new MySqlPageInterceptor(lcnMySQLProperty.getPageRegex());
    }

    /**
     * ShardingSphere 资源加载初始事件
     *
     * @return ShardingSphereResourceInitEvent
     */
    @Bean
    public ShardingSphereResourceInitEvent shardingSphereResourceInitEvent() {
        return new ShardingSphereResourceInitEvent(lcnMySQLProperty.getShardingInit());
    }

}
