package com.xx.jaseatschoicejava.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class FileUploadUtil {

    /**
     * 上传文件并返回文件名
     *
     * @param file     上传的文件
     * @param uploadDir 上传目录
     * @return 保存后的文件名
     */
    public static String uploadFile(MultipartFile file, String uploadDir) throws IOException {
        // 检查目录是否存在
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 生成新的文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        // 保存文件
        File dest = new File(uploadDir + fileName);
        file.transferTo(dest);

        return fileName;
    }

    /**
     * 上传图片并返回文件名
     *
     * @param file     上传的图片
     * @param uploadDir 上传目录
     * @return 保存后的文件名
     * @throws IOException 文件上传异常
     */
    public static String uploadImage(MultipartFile file, String uploadDir) throws IOException {
        // 验证图片类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("文件不是有效的图片类型");
        }

        return uploadFile(file, uploadDir);
    }
}
