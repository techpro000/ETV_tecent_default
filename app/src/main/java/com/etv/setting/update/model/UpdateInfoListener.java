package com.etv.setting.update.model;

import com.etv.setting.update.entity.UpdateInfo;

import java.util.List;

public interface UpdateInfoListener {

    void getUpdateInfoSuccess(UpdateInfo listCache);

    void overApp(String desc);


}
