package com.vivi.auth.web;

//import com.vivi.auth.config.JwtProperties;
//import com.vivi.auth.service.AuthService;
import com.vivi.auth.config.JwtProperties;
import com.vivi.auth.service.AuthService;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.utils.CookieUtils;
import com.vivi.entity.UserInfo;
import com.vivi.utils.JwtUtils;
import com.vivi.utils.RsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;

    @Value("${dg.jwt.cookieName}")
    private String cookieName;
    @Autowired
    private JwtProperties prop;

    /**
     * 登录授权功能
     * @param username
     * @param password
     * @return
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(@RequestParam("username")String username,
                                      @RequestParam("password")String password,
                                      HttpServletResponse response, HttpServletRequest request){
//        生成token
        String token = authService.login(username,password);
        if (StringUtils.isBlank(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
//        写入cookie token
        CookieUtils.setCookie(request, response,prop.getCookieName() , token, false);
//        response.addCookie(new Cookie("DG_TOKEN",token));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 验证token信息，并更新token时效。
     * @param token
     * @return
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(@CookieValue("DG_TOKEN")String token,
                                               HttpServletRequest request,
                                               HttpServletResponse response){
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            // 解析成功要重新生成token
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            // 更新cookie中的token
            CookieUtils.setCookie(request, response, prop.getCookieName(), newToken,false);
            return ResponseEntity.ok(userInfo);
        }catch (Exception e){
            throw new DgException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
