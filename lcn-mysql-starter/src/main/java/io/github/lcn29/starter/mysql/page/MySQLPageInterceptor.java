package io.github.lcn29.starter.mysql.page;

import io.github.lcn29.starter.mysql.page.model.Page;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * <pre>
 * MySQL MyBatis 分页拦截器
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 22:00
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class MySQLPageInterceptor implements Interceptor {

    /**
     * 属性 key, 可以通过对应的 key 从 MetaObject 对象中间接获取到 Statement 对应的属性
     */
    private final static String MAPPED_STATEMENT_ATTR_NAME = "delegate.mappedStatement";
    private final static String DELEGATE_PARAMETER_HANDLER_ATTR_NAME = "delegate.parameterHandler";
    private final static String DELEGATE_BOUND_SQL_SQL_ATTR_NAME = "delegate.boundSql.sql";

    /**
     * 需要用到的字符
     */
    private final static String SEMICOLON = ";";
    private final static String EMPTY_STRING = "";
    private final static String BLANK_SPACE = " ";
    private final static String COMMA = ",";

    /**
     * order by 关键字
     */
    private final static String ORDER_BY_LOWER_CASE = "order by";
    private final static String ORDER_BY_UPPER_CASE = "ORDER BY";

    /**
     * 统计总条数的 SQL
     */
    private final static String COUNT_SQL = "select count(1) from (%s) tmp";

    /**
     * limit 字段
     */
    private final static String LIMIT = "limit";

    /**
     * 分页方法名的格式
     */
    private final String pageSqlId;

    public MySQLPageInterceptor(String pageSqlId) {
        this.pageSqlId = pageSqlId;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (!(invocation.getTarget() instanceof RoutingStatementHandler)) {
            return invocation.proceed();
        }


        RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(MAPPED_STATEMENT_ATTR_NAME);

        // 方法名需要匹配入参的分页 ID
        String sqlId = mappedStatement.getId();
        if (sqlId == null || sqlId.isEmpty() || !sqlId.matches(pageSqlId)) {
            return invocation.proceed();
        }

        // 需要执行的 SQL 的包装对象
        BoundSql boundSql = statementHandler.getBoundSql();
        // 参数列表
        Map<?, ?> parameterObject = (Map<?, ?>) boundSql.getParameterObject();

        // 从参数列表中获取分页对象
        Page page = null;

        for (Object value : parameterObject.values()) {
            if (value instanceof Page) {
                page = (Page) value;
                break;
            }
        }

        if (page == null) {
            throw new ExecutorException("There was no Page Param in statement" + sqlId);
        }

        // 原始 SQL, 如果原始 SQL 包含 ; 将其替换掉
        String originSql = boundSql.getSql().replace(SEMICOLON, EMPTY_STRING);

        // 需要统计总条数
        if (page.getNeedCount()) {
            Connection connection = (Connection) invocation.getArgs()[0];
            getSqlCount(originSql, connection, metaObject, page);
        }

        metaObject.setValue(DELEGATE_BOUND_SQL_SQL_ATTR_NAME, generatePageResultSql(originSql, page));
        return invocation.proceed();
    }

    /**
     * 获取 SQL 的总条数, 并设置到 Page 对象的 totalCount 属性中
     *
     * @param originSql  原始 SQL
     * @param connection 数据库连接对象
     * @param metaObject RoutingStatementHandler 的元数据对象
     * @param page       请求参数的 Page 对象
     */
    private void getSqlCount(String originSql, Connection connection, MetaObject metaObject, Page page) throws SQLException {

        String tmpSql = originSql;

        // 小优化, 统计总条数的 SQL 可以不用排序
        // 如果原 SQL 中有 ORDER BY/ORDER BY, 将其省略
        if (originSql.contains(ORDER_BY_LOWER_CASE)) {
            tmpSql = originSql.split(ORDER_BY_LOWER_CASE)[0];
        }
        if (originSql.contains(ORDER_BY_UPPER_CASE)) {
            tmpSql = originSql.split(ORDER_BY_UPPER_CASE)[0];
        }

        // 拼接出统计总数的 SQL
        String countSql = String.format(COUNT_SQL, tmpSql);
        // 产生执行 SQL 对象
        PreparedStatement countStatement = connection.prepareStatement(countSql);
        // 获取执行 sql 的参数
        ParameterHandler parameterHandler = (ParameterHandler) metaObject.getValue(DELEGATE_PARAMETER_HANDLER_ATTR_NAME);
        // 设置参数
        parameterHandler.setParameters(countStatement);
        // 执行 SQL
        ResultSet resultSet = countStatement.executeQuery();
        int count = 0;
        if (resultSet.next()) {
            count = resultSet.getInt(1);
        }
        resultSet.close();
        countStatement.close();
        // 设置总条数到 page 对象的 totalCount 中
        page.setTotalCount(count);
    }

    /**
     * 拼接分页 SQL
     *
     * @param sql  原始的 SQL
     * @param page 分页对象
     * @return 最终的分页 SQL
     */
    private String generatePageResultSql(String sql, Page page) {

        int position = (page.getPageNum() - 1) * page.getPageSize();

        // 需要统计总条数, 偏移量就是需要的条数
        // 不需要总条数, 偏移量 = 需要的条数 + 1 (多查询 1 条数据，用于确定是否为最后一页)
        int offset = page.getNeedCount() ? page.getPageSize() : page.getPageSize() + 1;

        StringBuilder stringBuilder = new StringBuilder(sql).append(BLANK_SPACE).append(LIMIT).append(BLANK_SPACE)
            .append(position).append(COMMA).append(offset);
        return stringBuilder.toString();
    }

}

