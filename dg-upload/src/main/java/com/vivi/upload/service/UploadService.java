package com.vivi.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.domain.ThumbImageConfig;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@EnableConfigurationProperties(UploadProperties.class)
@Service
@Slf4j
public class UploadService {

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties uploadProperties;
    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/jpg","image/bmp");

    public String uploadImage(MultipartFile file){
        try{

//            校验文件类型
            String contentType = file.getContentType();
            if (!ALLOW_TYPES.contains(contentType)){
                throw new DgException(ExceptionEnum.INVALOD_FILE_TYPE);
            }
//            校验文件内容 ImageIO读到的不是图片会是null
            BufferedImage read = ImageIO.read(file.getInputStream());
            if (read == null){
                throw new DgException(ExceptionEnum.INVALOD_FILE_TYPE);
            }
//            这样截如果有两个.就完蛋
//            String typeName = file.getOriginalFilename().split("\\.")[1];
            String typeName = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), typeName, null);
            return uploadProperties.getBaseUrl() + storePath.getFullPath();
        }catch (IOException e){
            log.error("上传文件失败",e);
            throw new DgException(ExceptionEnum.UPLOAD_FILE_ERROR);
        }
    }
}
