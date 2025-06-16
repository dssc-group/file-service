package com.bupt.fileservice.controller;


import com.bupt.fileservice.pojo.Result;
import com.bupt.fileservice.service.MinioService;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FileDownloadController {

    @Autowired
    private MinioService minioService;

    @Value("${minio.bucket.name:tasks}")
    private String bucketName;

    private static final Map<String, String> MIME_TYPES = Map.of(
            "ppt","application/pdf",
//            "ppt", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "mp4", "video/mp4",
            "mp3", "audio/mpeg"
    );

    @GetMapping("/file/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        try {
            // 检索对象元数据
            StatObjectResponse stat = minioService.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            // 获取 InputStream
            InputStream inputStream = minioService.getFile(bucketName, fileName);
            Resource resource = new InputStreamResource(inputStream);

            String contentType = stat.contentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .contentLength(stat.size())
                    .body(resource);

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/file/video_info")
    public Result getVideoInfo(@RequestParam String fileName) {
        try {
            StatObjectResponse stat = minioService.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            Map<String, Object> videoInfo = new HashMap<>();
            videoInfo.put("fileName", fileName);
            videoInfo.put("fileSize", stat.size());
            videoInfo.put("contentType", stat.contentType());

            return Result.success(videoInfo);

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return Result.error("视频未找到！");
        }
    }

    @GetMapping("/file/video")
    public ResponseEntity<Resource> downloadVideo(@RequestParam  String fileName,
                                                  @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            StatObjectResponse stat = minioService.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            long fileSize = stat.size();
            String contentType = stat.contentType() != null ? stat.contentType() : "video/mp4";

            long rangeStart = 0;
            long rangeEnd = fileSize - 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                try {
                    rangeStart = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
                }
            }

            if (rangeStart > rangeEnd || rangeEnd >= fileSize) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
            }

            long contentLength = rangeEnd - rangeStart + 1;

            // 检索部分内容
            InputStream inputStream = minioService.getFile(bucketName, fileName);
            inputStream.skip(rangeStart);
            Resource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize);
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .contentLength(contentLength)
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/file/showFile")
    public ResponseEntity<Resource> showFile(@RequestParam String fileType, @RequestParam String fileName) {
        try {
            // 验证文件名
//            if (!isValidFileName(fileName)) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
//            }

            // 检索对象元数据
            StatObjectResponse stat = minioService.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );

            // 获取 InputStream
            InputStream inputStream = minioService.getFile(bucketName, fileName);
            Resource resource = new InputStreamResource(inputStream);

            String contentType = getContentType(fileType);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .contentLength(stat.size())
                    .body(resource);

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    private String getContentType(String fileType) {
        return MIME_TYPES.getOrDefault(fileType.toLowerCase(), "application/octet-stream");
    }

    private boolean isValidFileName(String fileName) {
        return fileName != null && fileName.matches("^[a-zA-Z0-9_.-]+$");
    }
}
