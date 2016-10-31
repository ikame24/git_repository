package com.encryption.biz;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cy002 on 2016/10/2.
 */
public class DataProvider {
    private static final String TAG = "DataProvider";

    public static final String ENCRYPTION_FLAG = "#";

    private static List<FolderInfo> folderList;

    // SD卡根目录
    public static String ROOTPAHT = Environment.getExternalStorageDirectory().getPath();

    // 图片后缀
    public static List<String> PIC_SUFFIXS = new ArrayList<String>();

    static {
        PIC_SUFFIXS.add("jpg");
        PIC_SUFFIXS.add("jpeg");
        PIC_SUFFIXS.add("png");
    }

    /** 操作回调方法 */
    public interface HandleCallback {
        public void onStart();

        public void onProgress(int curr, int total);

        public void onFinish();
    }

    /** 进度回调方法 */
    public interface FileProgressCallback {
        public void onProgress(int curr, int total);
    }

    /**
     * 文件夹解密类型
     */
    public static enum EncryptionType {
        NONE(1), HALF(2), ALL(3);

        private int value;

        EncryptionType(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }


    /**
     * 获取图片目录数据
     *
     * @return
     */
    public static List<FolderInfo> getFolders() {
        folderList = new ArrayList<FolderInfo>();

        FolderInfo folder1 = new FolderInfo("Download", "/Download/");
        FolderInfo folder2 = new FolderInfo("Browser", "/Download/Browser");
        FolderInfo folder3 = new FolderInfo("news_article", "/news_article/");
        FolderInfo folder4 = new FolderInfo("Pictures", "/Pictures/未命名相册");

        folderList.add(folder1);
        folderList.add(folder2);
        folderList.add(folder3);
        folderList.add(folder4);

        return folderList;
    }

    /**
     * 根据名称从列表中获取目录信息
     *
     * @param name
     * @return
     */
    public static String getFolderPathByName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }

        if (folderList == null || folderList.size() == 0) {
            return "";
        }

        for (int i = 0; i < folderList.size(); i++) {
            FolderInfo temp = folderList.get(i);
            if (name.equals(temp.getName())) {
                return temp.getPath();
            }
        }

        return "";
    }

    /**
     * 获取文件夹下的文件列表
     *
     * @param folderPath
     * @return
     */
    public static List<String> getAllFileList(String folderPath) {
        List<String> fileList = null;

        // 检查输入参数是否为空
        if (TextUtils.isEmpty(folderPath)) {
            Log.e(TAG, "getAllFileList() 输入参数为空folderPath=" + folderPath);
            return fileList;
        }

        // 检查输入目录文件是否存在
        File folder = new File(ROOTPAHT, folderPath);
        if (!folder.exists()) {
            Log.e(TAG, "getAllFileList() 输入文件夹不存在");
            return fileList;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            Log.e(TAG, "getAllFileList() 文件夹下文件个数为空");
            return fileList;
        }
        Log.i(TAG, "getAllFileList() 文件个数=" + files.length);

        // 是图片，获取加密文件
        fileList = new ArrayList<String>();

        for (int i = 0; i < files.length; i++) {
            File temp = files[i];

            boolean isImageFile = isPicture(temp.getPath());
            boolean isEncryptionFile = temp.getPath().indexOf(DataProvider.ENCRYPTION_FLAG) > -1;

            if (isImageFile || isEncryptionFile) {
                fileList.add(temp.getPath());
            }
        }

        return sortFileList(fileList);
    }

    /**
     * 获取文件夹下的文件列表
     *
     * @param folderPath
     * @return
     */
    public static List<String> getFileByPage(String folderPath, int page, int pageSize) {
        List<String> fileList = null;

        // 检查输入参数是否为空
        if (TextUtils.isEmpty(folderPath)) {
            Log.e(TAG, "getAllFileList() 输入参数为空folderPath=" + folderPath);
            return fileList;
        }

        // 检查输入目录文件是否存在
        File folder = new File(ROOTPAHT, folderPath);
        if (!folder.exists()) {
            Log.e(TAG, "getAllFileList() 输入文件夹不存在");
            return fileList;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            Log.e(TAG, "getAllFileList() 文件夹下文件个数为空");
            return fileList;
        }
        Log.i(TAG, "getAllFileList() 文件个数=" + files.length);

        List<File> pictureList = getPictureFiles(folder);
        pictureList = sortFiles(pictureList);

        // 是图片，获取加密文件
        fileList = new ArrayList<String>();
        // 文件个数
        int fileSize = pictureList.size();
        Log.i(TAG, "---->文件个数=" + fileList);

        // 总页数
        int maxPage = 0;
        if(fileSize % pageSize == 0) {
            maxPage = fileSize / pageSize;
        } else {
            maxPage = fileSize / pageSize + 1;
        }
        Log.i(TAG, "---->总页数=" + maxPage);

        if(page > maxPage) {
            page = maxPage;
        } else if(page == 0) {
            page = 1;
        }
        Log.i(TAG, "---->当前页数=" + page);

        int startIndex = (page - 1) * pageSize;
        int endIndex = page * pageSize>fileSize?fileSize:page*pageSize;
        Log.i(TAG, "---->获取位置, 开始=" + startIndex + ", 结束=" + endIndex);

        for (int i = startIndex; i < endIndex; i++) {
            File temp = pictureList.get(i);

            fileList.add(temp.getPath());
        }

        Log.i(TAG, "---->返回文件数=" + fileList.size());

        return fileList;
    }

    /**
     * 获取图片文件列表(包括加密的文件)
     * @param folder
     * @return
     */
    private static List<File> getPictureFiles(File folder) {
        List<File> pictureList = new ArrayList<File>();

        File[] files = folder.listFiles();
        for(int i=0; i<files.length; i++) {
            File temp = files[i];
            boolean isEncryptionFile = temp.getPath().indexOf(ENCRYPTION_FLAG)>-1;
            boolean isPictureFile = isPicture(temp.getPath());

            if(isEncryptionFile || isPictureFile) {
                pictureList.add(temp);
            }
        }

        return pictureList;
    }

    /**
     * 获取最大页码
     * @param folderPath
     * @param pageSize
     * @return
     */
    public static int getPageCount(String folderPath, int pageSize) {
        List<String> fileList = null;

        // 检查输入参数是否为空
        if (TextUtils.isEmpty(folderPath)) {
            Log.e(TAG, "getAllFileList() 输入参数为空folderPath=" + folderPath);
            return 0;
        }

        // 检查输入目录文件是否存在
        File folder = new File(ROOTPAHT, folderPath);
        if (!folder.exists()) {
            Log.e(TAG, "getAllFileList() 输入文件夹不存在");
            return 0;
        }

        List<File> pictureList = getPictureFiles(folder);
        int maxPage = (pictureList.size()%pageSize==0)?pictureList.size()/pageSize:pictureList.size()/pageSize+1;
        Log.i(TAG, "getPageCount() --->" + maxPage);

        return maxPage;
    }

    /**
     * 对文件列表进行排序，未加密在前，加密文件在后
     *
     * @param fileList
     * @return
     */
    public static List<String> sortFileList(List<String> fileList) {
        if (fileList == null || fileList.size() == 0) {
            Log.e(TAG, "sortFileList() 输入文件列表为空");
            return fileList;
        }

        List<String> result = new ArrayList<String>();
        List<String> imgList = new ArrayList<String>();
        List<String> encryptionList = new ArrayList<String>();

        for (String path : fileList) {

            boolean isEncryption = path.indexOf(DataProvider.ENCRYPTION_FLAG) > -1;
            if (isEncryption) {
                encryptionList.add(path);
            } else {
                boolean isPicture = isPicture(path);
                if (isPicture) {
                    imgList.add(path);
                }
            }
        }

        Log.i(TAG, "sortFileList() 排序后的文件imgList=" + imgList.size()
                + ", encryptionList=" + encryptionList.size());

        result.addAll(imgList);
        result.addAll(encryptionList);
        return result;
    }


    /**
     * 对文件列表进行排序，未加密在前，加密文件在后
     *
     * @param fileList
     * @return
     */
    public static List<File> sortFiles(List<File> fileList) {
        if (fileList == null || fileList.size() == 0) {
            Log.e(TAG, "sortFiles() 输入文件列表为空");
            return fileList;
        }

        List<File> result = new ArrayList<File>();
        List<File> imgList = new ArrayList<File>();
        List<File> encryptionList = new ArrayList<File>();

        for (File file : fileList) {
            String path = file.getPath();

            boolean isEncryption = path.indexOf(DataProvider.ENCRYPTION_FLAG) > -1;
            if (isEncryption) {
                encryptionList.add(file);
            } else {
                boolean isPicture = isPicture(path);
                if (isPicture) {
                    imgList.add(file);
                }
            }
        }

        Log.i(TAG, "sortFiles() 排序后的文件imgList=" + imgList.size()
                + ", encryptionList=" + encryptionList.size());

        result.addAll(imgList);
        result.addAll(encryptionList);
        return result;
    }

    /**
     * 获取文件名称
     *
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        // 参数是否为空
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "getFileName() 输入参数weikong");
            return "";
        }

        int index = path.indexOf(DataProvider.ENCRYPTION_FLAG);
        int subIndex = path.lastIndexOf(File.separator);

        boolean isPicture = isPicture(path);
        boolean isEncryptionFile = index > -1;

        if (isEncryptionFile) {
            return path.substring(subIndex + 1, index);
        }

        if (isPicture) {
            return path.substring(subIndex + 1, path.length());
        }

        return "";
    }

    /**
     * 判断是否是图片
     *
     * @param picPath
     * @return
     */
    public static boolean isPicture(String picPath) {
        if (TextUtils.isEmpty(picPath)) {
            return false;
        }

        int index = picPath.lastIndexOf(".");
        if (index > -1) {
            String suffix = picPath.substring(index + 1, picPath.length());

            return DataProvider.PIC_SUFFIXS.contains(suffix);
        }

        return false;
    }


}
