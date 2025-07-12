package com.lesson.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@NoArgsConstructor
public class PageResult implements Serializable {

    private long total; //总记录数

    private List records; //当前页数据集合

    private java.util.Map<String, Long> statusCounts; // 各状态数量统计

    public PageResult(long total, List records, java.util.Map<String, Long> statusCounts) {
        this.total = total;
        this.records = records;
        this.statusCounts = statusCounts;
    }

    public PageResult(long total, List records) {
        this.total = total;
        this.records = records;
        this.statusCounts = null;
    }

}
