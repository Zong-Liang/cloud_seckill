package com.seckill.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果封装
 *
 * @param <T> 数据类型
 * @author seckill
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据
     */
    private List<T> records;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 创建分页结果
     *
     * @param total   总记录数
     * @param records 当前页数据
     * @param <T>     数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    /**
     * 创建分页结果（带分页信息）
     *
     * @param total    总记录数
     * @param records  当前页数据
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param <T>      数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Long total, List<T> records, Integer pageNum, Integer pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) Math.ceil((double) total / pageSize));
        return result;
    }

    /**
     * 创建空分页结果
     *
     * @param <T> 数据类型
     * @return 空分页结果
     */
    public static <T> PageResult<T> empty() {
        PageResult<T> result = new PageResult<>();
        result.setTotal(0L);
        result.setRecords(Collections.emptyList());
        result.setPages(0);
        return result;
    }

    /**
     * 是否有下一页
     *
     * @return true-有下一页
     */
    public boolean hasNext() {
        return pageNum != null && pages != null && pageNum < pages;
    }

    /**
     * 是否有上一页
     *
     * @return true-有上一页
     */
    public boolean hasPrevious() {
        return pageNum != null && pageNum > 1;
    }
}
