package com.etv.util.down;

public interface SaveDateToDbListener {
    /**
     * 是否把数据村放入服务器了。
     * @param isSaveOk
     */
    void saveDataToDbOk(Boolean isSaveOk);
}
