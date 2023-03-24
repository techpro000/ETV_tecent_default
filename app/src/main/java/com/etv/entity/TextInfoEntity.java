package com.etv.entity;


/***
 * 字体显示
 */
public class TextInfoEntity {

    String textDesc;
    int textColor;

    public TextInfoEntity(String textDesc, int textColor) {
        this.textDesc = textDesc;
        this.textColor = textColor;
    }

    public String getTextDesc() {
        return textDesc;
    }

    public void setTextDesc(String textDesc) {
        this.textDesc = textDesc;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    public String toString() {
        return "TextInfoEntity{" +
                "textDesc='" + textDesc + '\'' +
                ", textColor=" + textColor +
                '}';
    }
}
