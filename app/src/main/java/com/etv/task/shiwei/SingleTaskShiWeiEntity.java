package com.etv.task.shiwei;

import com.etv.task.entity.MediAddEntity;

import java.util.List;

/**
 * 单机模式封装的实体类
 */
public class SingleTaskShiWeiEntity {

    List<MediAddEntity> list_image;   //包含所有的图片
    List<MediAddEntity> list_video;   //包含所有的视频
    List<MediAddEntity> listsEntity;  //包含所有的信息

    public List<MediAddEntity> getList_image() {
        return list_image;
    }

    public void setList_image(List<MediAddEntity> list_image) {
        this.list_image = list_image;
    }

    public List<MediAddEntity> getList_video() {
        return list_video;
    }

    public void setList_video(List<MediAddEntity> list_video) {
        this.list_video = list_video;
    }

    public List<MediAddEntity> getListsEntity() {
        return listsEntity;
    }

    public void setListsEntity(List<MediAddEntity> listsEntity) {
        this.listsEntity = listsEntity;
    }

    @Override
    public String toString() {
        return "SingleTaskShiWeiEntity{" +
                "list_image=" + list_image +
                ", list_video=" + list_video +
                ", listsEntity=" + listsEntity +
                '}';
    }
}
