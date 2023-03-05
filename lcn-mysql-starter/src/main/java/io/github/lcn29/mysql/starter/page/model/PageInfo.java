package io.github.lcn29.mysql.starter.page.model;

import java.util.List;

/**
 * <pre>
 * 分页结果封装对象
 * </pre>
 *
 * @author lcn29
 * @date 2023-03-05 22:23
 */
public class PageInfo<T> {

    /**
     * 总条数
     */
    private int totalCount;

    /**
     * 当前的页数
     */
    private int curPageNum;

    /**
     * 总页数
     */
    private int totalPageNum;

    /**
     * 是否为第一页
     */
    private boolean first;

    /**
     * 是否为最后一页
     */
    private boolean last;

    /**
     * 分页数据
     */
    private List<T> list;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCurPageNum() {
        return curPageNum;
    }

    public void setCurPageNum(int curPageNum) {
        this.curPageNum = curPageNum;
    }

    public int getTotalPageNum() {
        return totalPageNum;
    }

    public void setTotalPageNum(int totalPageNum) {
        this.totalPageNum = totalPageNum;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
