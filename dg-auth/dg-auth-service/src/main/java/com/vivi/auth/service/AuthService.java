package com.vivi.auth.service;

import com.vivi.auth.client.UserClient;
import com.vivi.auth.client.UserClient;
import com.vivi.auth.config.JwtProperties;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.entity.UserInfo;
import com.vivi.user.pojo.User;
import com.vivi.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;

    public String login(String username, String password) {
        try{
            User user = userClient.queryUserByUNAndPW(username, password);
            if (user == null){
                throw new DgException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
            }
        //        生成token
            UserInfo userInfo = new UserInfo(user.getId(),username);
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            return token;
        }catch (Exception e){
            log.error("[授权中心] 用户名或密码错误，用户名称:{}",username);
            throw new DgException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
        }

    }
}
