package com.vivi.order.config;

import com.github.wxpay.sdk.WXPayConfig;
import lombok.Data;

import java.io.InputStream;

@Data
public class PayConfig implements WXPayConfig {

    private String AppID;

    private String MchID;

    private String Key;

    private InputStream CertStream;

    private int HttpConnectTimeoutMs;

    private int HttpReadTimeoutMs;

    private String NotifyUrl;
}
