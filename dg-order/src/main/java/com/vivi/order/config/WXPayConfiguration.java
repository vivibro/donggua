package com.vivi.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WXPayConfiguration {

//    将属性注入配置文件
    @Bean
    @ConfigurationProperties(prefix = "dg.pay")
    public PayConfig payConfig(){
        return new PayConfig();
    }

//    将配置文件注入到WXPay
    @Bean
    public WXPay wxpay(PayConfig payConfig){
        return new WXPay(payConfig, WXPayConstants.SignType.MD5);
    }
}
