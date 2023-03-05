package io.github.lcn29.starter.mysql.page;

import io.github.lcn29.starter.mysql.page.model.Page;
import io.github.lcn29.starter.mysql.page.model.PageInfo;

import java.util.List;

/**
 * <pre>
 * 分页结果 PageInfo 协助类
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 22:26
 */
public class PageInfoHelper {

    /**
     * int 0
     */
    private final static int INT_ZERO = 0;

    /**
     * int 1
     */
    private final static int INT_ONE = 1;

    /**
     * 构建 PageInfo 对象
     *
     * @param page 请求参数 page
     * @param list 响应结果列表
     * @return PageInfo 对象
     */
    public static <T> PageInfo<T> buildPageInfo(Page page, List<T> list) {

        PageInfo<T> pageInfo = new PageInfo<>();
        // 查询的是第一页
        pageInfo.setFirst(page.getPageNum() == INT_ONE);
        // 当前的页数
        pageInfo.setCurPageNum(page.getPageNum());

        // 需要统计总条数
        if (page.getNeedCount()) {
            pageInfo.setList(list);
            pageInfo.setTotalCount(page.getTotalCount());
            pageInfo.setTotalPageNum(getTotalPageNum(page.getTotalCount(), page.getPageSize()));
            pageInfo.setLast(pageInfo.getCurPageNum() >= pageInfo.getTotalPageNum());
            return pageInfo;
        }

        // 不需要统计总条数

        // 因为不统计总条数的分页会查询多一条, 用来判断是否有下一页
        // 所以查询出来的数据大于查询的条数, 需要截取最后一条
        if (list.size() > page.getPageSize()) {
            pageInfo.setList(list.subList(INT_ZERO, list.size() - INT_ONE));
        } else {
            pageInfo.setList(list);
        }
        pageInfo.setLast(list.size() > page.getPageSize());
        return pageInfo;
    }

    /**
     * 获取总页数
     *
     * @param totalCount 总条数
     * @param pageSize   分页每页的条数
     * @return 总页数
     */
    private static int getTotalPageNum(int totalCount, int pageSize) {

        // 总条数小于等于 0, 直接默认第一页
        if (totalCount <= INT_ZERO) {
            return INT_ONE;
        }

        // 获取分页的页数
        int pageNum = totalCount / pageSize;
        // 如果还有多余的条数, 但是不满一页, 算一页
        if (totalCount % pageSize > INT_ONE) {
            pageNum++;
        }
        return pageNum;
    }

}
