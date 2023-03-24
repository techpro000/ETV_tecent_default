package com.etv.db;

import com.etv.entity.PoliceNumEntity;

import org.litepal.LitePal;

import java.util.List;

public class DbPoliceUtil {

    public static boolean savePoliceNumToLocal(PoliceNumEntity policeNumEntity) {
        if (policeNumEntity == null) {
            return false;
        }
        try {
            return policeNumEntity.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<PoliceNumEntity> getPoliceList() {
        try {
            List<PoliceNumEntity> list = LitePal.findAll(PoliceNumEntity.class);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean clearAllPoliceInfo() {
        boolean isTrue = false;
        int statues = LitePal.deleteAll(PoliceNumEntity.class);
        if (statues > 0) {
            isTrue = true;
        }
        return isTrue;
    }

}
