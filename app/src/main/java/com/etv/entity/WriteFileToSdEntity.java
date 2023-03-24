package com.etv.entity;

/**
 * 将离线节目写入到SD卡的实体类
 */

public class WriteFileToSdEntity {

    int taskId;
    int pmId;
    String fileName;

    public WriteFileToSdEntity(int taskId, int pmId, String fileName) {
        this.taskId = taskId;
        this.pmId = pmId;
        this.fileName = fileName;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getPmId() {
        return pmId;
    }

    public void setPmId(int pmId) {
        this.pmId = pmId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "WriteFileToSdEntity{" +
                "taskId=" + taskId +
                ", pmId=" + pmId +
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
