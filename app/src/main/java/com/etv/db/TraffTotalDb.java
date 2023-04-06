package com.etv.db;

import android.util.Log;

import com.etv.util.net.AppTrafficModel;

import org.litepal.LitePal;

import java.util.List;

public class TraffTotalDb {

    /**
     * 保存数据库
     *
     * @param entity
     * @return
     */
    public static boolean saveTraffTotalToLocal(AppTrafficModel entity) {
        boolean isSave = false;
        if (entity == null) {
            return isSave;
        }
        try {
            isSave = entity.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isSave;
    }

    /**
     * 获取统计列表
     *
     * @return
     */
    public static List<AppTrafficModel> getTraffInfoList() {
        List<AppTrafficModel> txtList = null;
        try {
            txtList = LitePal.findAll(AppTrafficModel.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (txtList == null || txtList.size() < 1) {
            Log.e("traff", "txtList=0");
        } else {
            Log.e("traff", "txtList=" + txtList.size());
        }
        return txtList;
    }


    /**
     * 删除用户数据
     *
     * @return
     */
    public static boolean delTraffInfoById(String saveId) {
        try {
            int delNum = LitePal.deleteAll(AppTrafficModel.class, "saveId = ?", saveId + "");
            if (delNum < 1) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void clearAllData() {
        int delnum = LitePal.deleteAll(AppTrafficModel.class);
        Log.e("traff", "清理数据库数据==" + delnum);
    }

}
