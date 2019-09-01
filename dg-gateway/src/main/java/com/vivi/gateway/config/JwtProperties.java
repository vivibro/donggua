package com.vivi.gateway.config;

import com.vivi.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;


@Data
@ConfigurationProperties("dg.jwt")
public class JwtProperties {


    private String pubKeyPath;// 公钥路径
    private String cookieName;
    private PublicKey publicKey; // 公钥


    private static final Logger logger = LoggerFactory.getLogger(JwtProperties.class);

    /**
     * @PostContruct：在构造方法执行之后执行该方法
     * 公钥和私钥放入内存中。
     */
    @PostConstruct
    public void init() throws Exception{
//       读取公钥
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
    }

}
