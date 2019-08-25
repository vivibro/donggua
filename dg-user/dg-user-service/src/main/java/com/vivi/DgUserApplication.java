package com.vivi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.vivi.user.mapper")
public class DgUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(DgUserApplication.class, args);
    }
}