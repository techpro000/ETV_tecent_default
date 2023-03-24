package com.etv.util.wps.excel;

import java.util.List;

/**
 * 封装EXCEL得实体类
 */
public class ExcelEntity {
    int nums;
    List<ExcelItemEntity> lists;

    public ExcelEntity(int nums, List<ExcelItemEntity> lists) {
        this.nums = nums;
        this.lists = lists;
    }

    public int getNums() {
        return nums;
    }

    public void setNums(int nums) {
        this.nums = nums;
    }

    public List<ExcelItemEntity> getLists() {
        return lists;
    }

    public void setLists(List<ExcelItemEntity> lists) {
        this.lists = lists;
    }
}
