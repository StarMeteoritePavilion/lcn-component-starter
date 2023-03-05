package io.github.lcn29.starter.mysql.page.model;

/**
 * <pre>
 * 分页请求对象
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 22:22
 */
public final class Page {

    /**
     * 分页的页数
     */
    private int pageNum;

    /**
     * 分页的每页条数
     */
    private int pageSize;

    /**
     * 是否需要统计总条数, 默认为 true
     */
    private boolean needCount;

    /**
     * 当需要查询总条数时, 会将总条数临时存放在这里
     */
    private int totalCount;

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean getNeedCount() {
        return needCount;
    }

    public void setNeedCount(boolean needCount) {
        this.needCount = needCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
