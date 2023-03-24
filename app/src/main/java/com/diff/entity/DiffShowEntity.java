package com.diff.entity;


public class DiffShowEntity {
    float widthChSize;    //屏幕压缩比例
    float heightChSize;   //屏幕压缩比例

    public DiffShowEntity(float widthChSize, float heightChSize) {
        this.widthChSize = widthChSize;
        this.heightChSize = heightChSize;
    }

    public float getWidthChSize() {
        return widthChSize;
    }

    public void setWidthChSize(float widthChSize) {
        this.widthChSize = widthChSize;
    }

    public float getHeightChSize() {
        return heightChSize;
    }

    public void setHeightChSize(float heightChSize) {
        this.heightChSize = heightChSize;
    }
}