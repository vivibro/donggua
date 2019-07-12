package com.vivi.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "dg.upload")
public class UploadProperties {
    private String baseUrl;

}
