package com.encryption.biz;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by hsj on 2016/10/12.
 */

public class FolderBiz {
    private static final String TAG = "FolderBiz";

    /**
     * 加密文件
     *
     * @param name
     * @param callback
     */
    public static void encodeFolder(final String name, final DataProvider.HandleCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (callback != null) {
                    callback.onStart();
                }

                String folderSubPath = DataProvider.getFolderPathByName(name);

                // 加密
                encryptionFolder(folderSubPath, new DataProvider.FileProgressCallback() {
                    @Override
                    public void onProgress(int curr, int total) {
                        if (callback != null) {
                            callback.onProgress(curr, total);
                        }
                    }
                });

                if (callback != null) {
                    callback.onFinish();
                }
            }
        }).start();
    }

    /**
     * 解密文件
     *
     * @param name
     * @param callback
     */
    public static void decodeFolder(final String name, final DataProvider.HandleCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if (callback != null) {
                    callback.onStart();
                }

                String folderSubPath = DataProvider.getFolderPathByName(name);

                // 解密
                decryptionFolder(folderSubPath, new DataProvider.FileProgressCallback() {
                    @Override
                    public void onProgress(int curr, int total) {
                        if (callback != null) {
                            callback.onProgress(curr, total);
                        }
                    }
                });

                if (callback != null) {
                    callback.onFinish();
                }
            }
        }).start();
    }


    /**
     * 加密目录下的文件
     *
     * @param path
     */
    public static void encryptionFolder(String path, DataProvider.FileProgressCallback callback) {
        // 检查输入目录是否为空
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "encryptionFolder() 输入的参数为空");
            return;
        }

        // 检查目录是否存在
        File folder = new File(DataProvider.ROOTPAHT, path);
        if (!folder.exists()) {
            Log.e(TAG, "encryptionFolder() 输入的文件不存在=" + folder.getPath());
            return;
        }

        // 获取目录下的文件
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            Log.e(TAG, "encryptionFolder() 输入的文件夹下没有文件");
            return;
        }

        // 修改文件后缀
        for (int i = 0; i < files.length; i++) {
            File temp = files[i];

            // 文件夹不加密
            if (!temp.isFile()) {
                continue;
            }

            // 是否图片
            if (!DataProvider.isPicture(temp.getPath())) {
                continue;
            }

            encryptionFile(temp);

            if (callback != null) {
                callback.onProgress((i + 1), files.length);
            }
        }
    }

    /**
     * 加密单个文件
     *
     * @param temp
     */
    public static boolean encryptionFile(File temp) {
        try {
            String newName = temp.getPath() + DataProvider.ENCRYPTION_FLAG + System.currentTimeMillis();
            Log.i(TAG, "encryptionFile() 原来的文件路径=" + temp.getPath() + ", 加密后的文件名称=" + newName);

            boolean isSucc = temp.renameTo(new File(newName));
            return isSucc;

        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;
    }


    /**
     * 加密目录下的文件
     *
     * @param path
     */
    public static void decryptionFolder(String path, DataProvider.FileProgressCallback callback) {
        // 检查输入目录是否为空
        if (TextUtils.isEmpty(path)) {
            Log.e(TAG, "encryptionFolder() 输入的参数为空");
            return;
        }

        // 检查目录是否存在
        File folder = new File(DataProvider.ROOTPAHT, path);
        if (!folder.exists()) {
            Log.e(TAG, "encryptionFolder() 输入的文件不存在=" + folder.getPath());
            return;
        }

        // 获取目录下的文件
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            Log.e(TAG, "encryptionFolder() 输入的文件夹下没有文件");
            return;
        }

        // 修改文件后缀
        for (int i = 0; i < files.length; i++) {
            File temp = files[i];

            decryptionFile(temp);

            if (callback != null) {
                callback.onProgress((i + 1), files.length);
            }
        }
    }

    /**
     * 解密单个文件
     *
     * @param temp
     */
    public static boolean decryptionFile(File temp) {
        String newName = "";
        int index = temp.getPath().lastIndexOf(DataProvider.ENCRYPTION_FLAG);
        if (index > -1) {
            newName = temp.getPath().substring(0, index);
        }

        Log.i(TAG, "decryptionFile() 解密后的文件名称=" + newName);
        return temp.renameTo(new File(newName));
    }

    /**
     * 目录是否已经加密
     *
     * @param name
     * @return
     */
    public static DataProvider.EncryptionType isEncryption(String name) {
        // 检查输入参数是否为空
        if (TextUtils.isEmpty(name)) {
            Log.e(TAG, "isEncryption() 输入参数为空");
            return DataProvider.EncryptionType.NONE;
        }

        // 获取文件夹路径
        String folderSubPath = DataProvider.getFolderPathByName(name);

        if (TextUtils.isEmpty(folderSubPath)) {
            Log.e(TAG, "isEncryption() 获取路径为空=" + name);
            return DataProvider.EncryptionType.NONE;
        }

        String path = DataProvider.ROOTPAHT + folderSubPath;
        File folder = new File(path);

        if (!folder.exists()) {
            Log.e(TAG, "isEncryption() 文件不存在=" + path);
            return DataProvider.EncryptionType.NONE;
        }

        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            Log.e(TAG, "isEncryption() 文件夹为空=" + path);
            return DataProvider.EncryptionType.NONE;
        }

        int fileCount = files.length;
        int unEncryptionCount = 0;

        for (int i = 0; i < files.length; i++) {
            File temp = files[i];

            if (temp.isFile()) {
                boolean isUnEncryption = temp.getPath().indexOf(DataProvider.ENCRYPTION_FLAG) <= -1;
                boolean isImageFile = DataProvider.isPicture(temp.getPath());

                if (isUnEncryption && isImageFile) {
                    unEncryptionCount += 1;
                }
            }
        }

        DataProvider.EncryptionType result = null;

        if (unEncryptionCount > 0) {
            result = DataProvider.EncryptionType.HALF;
        } else {
            result = DataProvider.EncryptionType.ALL;
        }

        Log.i(TAG, "isEncryption() ---> name=" + name + ", 是否加密结果=" + result);
        return result;
    }

    /**
     * 文件是否已经加密
     *
     * @param filePath
     * @return
     */
    public static boolean isEncryptionFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            Log.e(TAG, "isEncryptionFile() 输入惨呼为空filePath=" + filePath);
            return false;
        }

        return filePath.indexOf(DataProvider.ENCRYPTION_FLAG) > -1;
    }


}
