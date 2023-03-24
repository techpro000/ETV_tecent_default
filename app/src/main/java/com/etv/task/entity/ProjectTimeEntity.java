//package com.etv.task.entity;
//
//
//import org.litepal.crud.DataSupport;
//
///**
// * 屏保时间控制
// */
//public class ProjectTimeEntity extends DataSupport {
//
//    long id;
//    String sencenFromId;
//    String backTime;
//    String sencenToId;
//
//    public ProjectTimeEntity(String sencenFromId, String backTime, String sencenToId) {
//        this.sencenFromId = sencenFromId;
//        this.backTime = backTime;
//        this.sencenToId = sencenToId;
//    }
//
//    public long getId() {
//        return id;
//    }
//
//    public void setId(long id) {
//        this.id = id;
//    }
//
//    public String getSencenFromId() {
//        return sencenFromId;
//    }
//
//    public void setSencenFromId(String sencenFromId) {
//        this.sencenFromId = sencenFromId;
//    }
//
//    public String getBackTime() {
//        return backTime;
//    }
//
//    public void setBackTime(String backTime) {
//        this.backTime = backTime;
//    }
//
//    public String getSencenToId() {
//        return sencenToId;
//    }
//
//    public void setSencenToId(String sencenToId) {
//        this.sencenToId = sencenToId;
//    }
//
//    @Override
//    public String toString() {
//        return "ProjectTimeEntity{" +
//                "id='" + id + '\'' +
//                "sencenFromId='" + sencenFromId + '\'' +
//                ", backTime='" + backTime + '\'' +
//                ", sencenToId='" + sencenToId + '\'' +
//                '}';
//    }
//}
