package com.bupt.fileservice.service;

import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@Slf4j
public class FileService {
    @Autowired
    private MinioService minioService;
    @Autowired
    private RedisQueueService redisQueueService;
    public int uploadTask(String fileName, MultipartFile videoFile) {
        String[] parts = fileName.split("/");
        String deadline = parts[4];
        int dotIndex = deadline.lastIndexOf(".");
        deadline = deadline.substring(0, dotIndex);
        LocalDateTime deadlineDateTime = LocalDateTime.parse(deadline);
        if(deadlineDateTime.isBefore(LocalDateTime.now())){
            return -2;
        }
        try {
            minioService.uploadFile("tasks", fileName, videoFile);
            redisQueueService.publishUploadStatus(fileName);
            return 0;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.info(e.getMessage());
            return -1;
        }
    }

    public int uploadPPTTask(String fileName, MultipartFile videoFile) {
        String[] parts = fileName.split("/");
        String deadline = parts[4];
        int dotIndex = deadline.lastIndexOf(".");
        deadline = deadline.substring(0, dotIndex);
        LocalDateTime deadlineDateTime = LocalDateTime.parse(deadline);
        if(deadlineDateTime.isBefore(LocalDateTime.now())){
            return -2;
        }
        try {
//            System.out.println(videoFile.getContentType());
            minioService.uploadFile("tasks", fileName, videoFile);
            redisQueueService.publishUploadStatus(fileName);
            return 0;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.info(e.getMessage());
            return -1;
        }
    }

    public int uploadTaskWithNoMessage(String fileName,MultipartFile file) {
        try {
//            System.out.println(videoFile.getContentType());
            minioService.uploadFile("tasks",fileName ,file);
            return 0;
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.info(e.getMessage());
            return -1;
        }
    }
}
