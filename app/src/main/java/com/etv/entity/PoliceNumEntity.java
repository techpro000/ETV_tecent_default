package com.etv.entity;

import org.litepal.crud.LitePalSupport;

/***
 * 报警电话数据库
 */
public class PoliceNumEntity extends LitePalSupport {

    String titleTag;
    String phoneNum;
    boolean isChooice;

    public PoliceNumEntity(String titleTag, String phoneNum, boolean isChooice) {
        this.titleTag = titleTag;
        this.phoneNum = phoneNum;
        this.isChooice = isChooice;
    }

    public boolean isChooice() {
        return isChooice;
    }

    public void setChooice(boolean chooice) {
        isChooice = chooice;
    }

    public String getTitleTag() {
        return titleTag;
    }

    public void setTitleTag(String titleTag) {
        this.titleTag = titleTag;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    @Override
    public String toString() {
        return "PoliceNumEntity{" +
                "titleTag='" + titleTag + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                '}';
    }
}
