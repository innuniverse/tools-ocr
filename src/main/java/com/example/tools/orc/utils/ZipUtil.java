package com.example.tools.orc.utils;

import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 文件解压相关工具类
 */
public class ZipUtil {

    public static List<MultipartFile> unZip(MultipartFile multipartFile) throws IOException {

        // 解压zip文件
        List<MultipartFile> multipartFileList = new ArrayList<>();
        ZipInputStream zipInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        String zipEntryName = null;
        try {
            zipInputStream = new ZipInputStream(multipartFile.getInputStream());
            bufferedInputStream = new BufferedInputStream(zipInputStream);
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null){
                zipEntryName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    continue;
                }
                // 文件名称为空抛错
                Assert.notNull(zipEntryName,"压缩文件中子文件的名字格式不正确");
                // 每个文件的流
                byte[] bytes = new byte[(int)zipEntry.getSize()];
                bufferedInputStream.read(bytes, 0, (int) zipEntry.getSize());
                InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                // MultipartFile itemMultipartFile = MockMultipartFile(zipEntryName, zipEntryName, "JPG", byteArrayInputStream);
                MultipartFile itemMultipartFile = MultipartFileUtil.getMultipartFile(byteArrayInputStream, zipEntryName);
                multipartFileList.add(itemMultipartFile);
                byteArrayInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bufferedInputStream!=null){
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (zipInputStream!=null){
                try {
                    zipInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return multipartFileList;
    }

    public static List<String> parseZipFile(MultipartFile zipFile) {
        String tempDri = System.getProperty("java.io.tmpdir");
        String tempZipDirPath = tempDri + "/tempFileCache/";
        String tempImageDirPath = tempDri + "/tempFileCache/";
        File file = new File(tempZipDirPath);
        // 如果文件夹不存在  创建文件夹
        if (!file.exists()) {
            file.mkdir();
        }
        // 获取文件名（包括后缀）
        String originalFileName = zipFile.getOriginalFilename();
        tempZipDirPath = tempZipDirPath + originalFileName;
        try {
            File dest = new File(tempZipDirPath);
            zipFile.transferTo(dest);
            // 获取解压出来原始文件名,不带后缀
            return ZipUtil.unZip(dest, tempImageDirPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    /**
     * zip解压
     *
     * @param srcDirPath     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static List<String> unZip(File srcDirPath, String destDirPath) throws RuntimeException {

        if ( !srcDirPath.exists() ) {
            throw new RuntimeException(srcDirPath.getPath() + "所指文件不存在");
        }

        List<String> fileNames = new ArrayList<>();
        ZipFile zipFile = null;
        try {
            long start = System.currentTimeMillis();
            zipFile = new ZipFile(srcDirPath);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String name = entry.getName();

                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = destDirPath + "/" + name;
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    fileNames.add(name);
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + name);
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    int len;
                    byte[] buf = new byte[1024];
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return fileNames;
    }

    public static boolean deleteFiles(List<String> fileDirPaths) {
        for (String fileDirPath : fileDirPaths) {
            deleteFile(fileDirPath);
        }
        return true;
    }

    /**
     * 删除文件
     *
     * @param fileDirPath 文件地址
     * @return 成功失败
     */
    public static boolean deleteFile(String fileDirPath) {
        boolean flag = false;
        File file = new File(fileDirPath);
        if (!file.exists()) {
            return flag;
        }
        if (!file.isDirectory()) {
            return flag;
        }

        String[] tempFiles = file.list();
        if (tempFiles == null) {
            return flag;
        }

        File temp;
        for (String tempFileName : tempFiles) {
            if (fileDirPath.endsWith(File.separator)) {
                temp = new File(fileDirPath + tempFileName);
            } else {
                temp = new File(fileDirPath + File.separator + tempFileName);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                // 先删除文件夹里面的文件
                deleteFile(fileDirPath + "/" + tempFileName);
                // 再删除空文件夹
                deleteFile(fileDirPath + "/" + tempFileName);
                flag = true;
            }
        }
        return flag;
    }

}

