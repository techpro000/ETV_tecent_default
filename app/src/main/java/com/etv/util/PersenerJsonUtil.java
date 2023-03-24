package com.etv.util;

import com.etv.db.DbPoliceUtil;
import com.etv.entity.PoliceNumEntity;

import org.json.JSONArray;

public class PersenerJsonUtil {

    public static void parsenerWarningPhones(String warningPhones) {
        logInfo("parsenerWarningPhones===" + warningPhones);
        if (warningPhones == null || warningPhones.length() < 5) {
            logInfo("传递==null");
            DbPoliceUtil.clearAllPoliceInfo();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(warningPhones);
            int num = jsonArray.length();
            logInfo("num==" + num);
            if (num < 1) {
                logInfo("num < 1 ==");
                return;
            }
            DbPoliceUtil.clearAllPoliceInfo();
            for (int i = 0; i < num; i++) {
                String phoneNum = jsonArray.getString(i);
                String tag = System.currentTimeMillis() + "";
                PoliceNumEntity policeNumEntity = new PoliceNumEntity(tag, phoneNum, false);
                boolean isSave = DbPoliceUtil.savePoliceNumToLocal(policeNumEntity);
                logInfo("便利电话号码 ： " + phoneNum + " /save = " + isSave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logInfo(String ogInfo) {
        MyLog.cdl("===解析设备==设置信息==========" + ogInfo);
    }

}
