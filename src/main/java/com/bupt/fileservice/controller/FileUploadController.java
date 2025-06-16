package com.bupt.fileservice.controller;

import com.bupt.fileservice.pojo.Result;
import com.bupt.fileservice.service.FileService;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class FileUploadController {
    @Autowired
    private FileService fileService;
    @PostMapping ("/file/uploadVideoTask") //上传视频
    public Result uploadVideoTask(@RequestParam(value = "File") MultipartFile File,
                                    @RequestParam(value = "videoFileName") String videoFileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {

        int code = fileService.uploadTask(videoFileName,File);
        if(code==-2){
            return Result.error("作业已截止");
        }
        return code==0?Result.success("File upload success!"):Result.error("File upload Failed！");
    }
    @PostMapping ("/file/uploadWordTask") //上传视频
    public Result uploadWordTask(@RequestParam(value = "File") MultipartFile File,
                                    @RequestParam(value = "fileName") String fileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {

        int code = fileService.uploadTask(fileName,File);
        if(code==-2){
            return Result.error("作业已截止");
        }
        return code==0?Result.success("File upload success!"):Result.error("File upload Failed！");
    }
    @PostMapping ("/file/uploadPPTTask") //上传视频
    public Result uploadPPTTask(@RequestParam(value = "File") MultipartFile File,
                                    @RequestParam(value = "fileName") String fileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {

        int code = fileService.uploadTask(fileName,File);
        if(code==-2){
            return Result.error("作业已截止");
        }
        return code==0?Result.success("File upload success!"):Result.error("File upload Failed！");
    }
    @PostMapping ("/file/uploadTeacherTask") //上传视频
    public Result uploadTeacherTask(@RequestParam(value = "File") MultipartFile File,
                                @RequestParam(value = "fileName") String fileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {

        int code = fileService.uploadTaskWithNoMessage(fileName,File);
        return code==0?Result.success("File upload success!"):Result.error("File upload Failed！");
    }

    @PostMapping("/file/uploadAudioTask")
    public Result uploadAudioTask(@RequestParam(value = "File") MultipartFile File,
                                    @RequestParam(value = "fileName") String fileName) throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {

        int code = fileService.uploadTask(fileName,File);
        if(code==-2){
            return Result.error("作业已截止");
        }
        return code==0?Result.success("File upload success!"):Result.error("File upload Failed！");
    }
}
