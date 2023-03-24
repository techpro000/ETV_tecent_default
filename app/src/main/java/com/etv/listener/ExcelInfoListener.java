package com.etv.listener;

import com.etv.util.wps.excel.ExcelEntity;

import java.util.List;

public interface ExcelInfoListener {
    void backExcelInfo(boolean isTrue, List<ExcelEntity> lists);
}