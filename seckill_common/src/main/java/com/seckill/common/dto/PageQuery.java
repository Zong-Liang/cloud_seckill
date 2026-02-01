package com.seckill.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基类
 *
 * @author seckill
 * @since 1.0.0
 */
@Data
public class PageQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 页码，默认第1页
     */
    @Min(value = 1, message = "页码最小为1")
    private Integer pageNum = 1;

    /**
     * 每页数量，默认10条
     */
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 10;

    /**
     * 获取偏移量（用于 MySQL LIMIT）
     *
     * @return 偏移量
     */
    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
