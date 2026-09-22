package com.zy.core.result;

import lombok.Data;

import java.util.List;

/**
 * 分页返回体。
 *
 * @param <T> 记录类型
 */
@Data
public class PageResult<T> {

    /** 当前页码（从 1 开始） */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总记录数 */
    private long total;

    /** 当前页记录 */
    private List<T> records;

    public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.setPageNum(pageNum);
        r.setPageSize(pageSize);
        r.setTotal(total);
        r.setRecords(records);
        return r;
    }
}
