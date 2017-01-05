package com.hm.bitmaploaddemo.utils;

import android.text.format.Formatter;

import com.hm.bitmaploaddemo.App;

import java.io.File;

/**
 * Created by dumingwei on 2016/9/30.
 */
public class CacheUtil {

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    public static String getAutoFileOrFilesSize(String filePath) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Formatter.formatFileSize(App.getInstance(), blockSize);
    }

    /**
     * 获取指定文件大小
     */
    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            size = file.length();
        }
        return size;
    }

    /**
     * 获取指定文件夹
     * @throws Exception
     */
    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File flist[] = f.listFiles();
        for (int i = 0; i < flist.length; i++) {
            if (flist[i].isDirectory()) {
                size = size + getFileSizes(flist[i]);
            } else {
                size = size + getFileSize(flist[i]);
            }
        }
        return size;
    }

    /**
     * 清空缓存
     * @param file
     * @return
     */
    public static boolean clearCache(File file) {
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                return file.delete();
            }
            for (File childFile : childFiles) {
                clearCache(childFile);
            }
            return file.delete();
        }
        return false;
    }
}
