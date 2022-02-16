package com.at.blog.controller;


import com.at.blog.utils.QiniuUtils;
import com.at.blog.vo.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("upload")
public class UploadController {
    @Autowired
    private QiniuUtils qiniuUtils;

    //@RequestParam Spring中用于接收文件
    @PostMapping
    public Result upload(@RequestParam("image") MultipartFile file){
        //原始文件名称 eg:xxx.png
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(originalFilename, ".");
        //上传文件上传到哪里呢？  七牛云:::云服务器按量付费速度快
        boolean upload = qiniuUtils.upload(file, fileName);
        if (upload){
            return Result.success(QiniuUtils.url+fileName);
        }
        return Result.fail(20001, "上传失败");

    }
}
