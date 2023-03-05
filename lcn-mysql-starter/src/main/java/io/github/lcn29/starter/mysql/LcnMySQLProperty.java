package io.github.lcn29.starter.mysql;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <pre>
 * 属性配置类
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 21:52
 */
@ConfigurationProperties(prefix = "lcn.mysql")
public class LcnMySQLProperty {

    /**
     * MyBatis 分页拦截器拦截的方法名表达式
     */
    private String pageRegex = ".*Page$";

    /**
     * sharding-jdbc 在项目启动时提前初始的 SQL
     */
    private String shardingInit = "select 'X'";

    public String getPageRegex() {
        return pageRegex;
    }

    public void setPageRegex(String pageRegex) {
        this.pageRegex = pageRegex;
    }

    public String getShardingInit() {
        return shardingInit;
    }

    public void setShardingInit(String shardingInit) {
        this.shardingInit = shardingInit;
    }
}
