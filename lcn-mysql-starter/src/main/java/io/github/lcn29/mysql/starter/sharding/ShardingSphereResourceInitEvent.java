package io.github.lcn29.mysql.starter.sharding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * <pre>
 * ShardingSphere 资源加载初始事件
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 22:56
 */
public class ShardingSphereResourceInitEvent implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(ShardingSphereResourceInitEvent.class);

    private final String shardingInit;

    private ApplicationContext applicationContext;

    public ShardingSphereResourceInitEvent(String shardingInit) {
        this.shardingInit = shardingInit;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        logger.info("lcn sharding: try to init resource");

        Map<String, DataSource> allDataSource = applicationContext.getBeansOfType(DataSource.class);

        // 尝试执行初始化 SQL
        for (DataSource value : allDataSource.values()) {
            try {
                // 第一次 ShardingSphere 在第一次执行 SQL 的时候有很慢 (1s 多的耗时)
                // 主要是会初始一下对象和 SQL 解析缓存
                // 这里手动再项目启动成功后, 执行一条简单的 SQL 让对象提前创建, 提高一些效率
                // SQL 解析缓存的方式, 暂时没找到
                Connection connection = value.getConnection();
                connection.prepareStatement(shardingInit).execute();
            } catch (SQLException sqlException) {
                logger.warn("ShardingSphereResourceInitEvent try to execute init sql error:", sqlException);
            }
        }
    }

}
