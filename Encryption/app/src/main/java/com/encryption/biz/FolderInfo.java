package com.encryption.biz;

/**
 * Created by cy002 on 2016/10/2.
 */
public class FolderInfo {

    private String name;

    private String path;

    private boolean isEncryption;


    /**
     * 构造方法
     * @param name
     * @param path
     */
    public FolderInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getIsEncryption() {
        return isEncryption;
    }

    public void setEncryption(boolean encryption) {
        isEncryption = encryption;
    }
}
