package com.atguigu.gmall0715.manage.controller;





import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;


import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin
public class FileUploadController {
    //对于服务器ip来讲，都应在应用程序中实现软编码，就是提前定义好，使用的时候直接调用就OK，不用重复的去编写
    @Value("${fileServer.url}")
    private String fileUrl;

    /**
     * 上传文件
     * @param file
     * @return
     * @throws IOException
     * @throws MyException
     */
    @RequestMapping("fileUpload")
    public String fileUpload(MultipartFile file)throws IOException, MyException {
        String imgurl = fileUrl;
        if (file != null){
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            ClientGlobal.init(configFile);
            TrackerClient trackerClient=new TrackerClient();
            TrackerServer trackerServer=trackerClient.getConnection();
            StorageClient storageClient=new StorageClient(trackerServer,null);
            String originalFilename = file.getOriginalFilename();
            String[] upload_file = storageClient.upload_file(file.getBytes(), "jpg", null);
            for (int i = 0; i < upload_file.length; i++) {
                String s = upload_file[i];
                //System.out.println("s = " + s);
                imgurl+="/"+s;
            }
        }

        return imgurl;
    }
}
