package com.cheersai.nexus.user.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果VO
 */
@Data
public class PageResultVO<T> {

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总数
     */
    private Long total;

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
    private Integer totalPages;

    public static <T> PageResultVO<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setList(list);
        result.setTotal(total);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotalPages((int) Math.ceil((double) total / pageSize));
        return result;
    }
}
