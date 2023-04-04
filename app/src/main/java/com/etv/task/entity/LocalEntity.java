package com.etv.task.entity;

/**
 * 本地下载文件
 */
public class LocalEntity {

    private String fileName;
    private String fileLength;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileLength() {
        return fileLength;
    }

    public void setFileLength(String fileLength) {
        this.fileLength = fileLength;
    }


    public LocalEntity(String fileName, String fileLength) {
        this.fileName = fileName;
        this.fileLength = fileLength;
    }
}
