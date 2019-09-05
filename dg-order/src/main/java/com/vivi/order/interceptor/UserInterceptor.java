package com.vivi.order.interceptor;

import com.vivi.order.config.JwtProperties;
import com.vivi.common.utils.CookieUtils;
import com.vivi.entity.UserInfo;
import com.vivi.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    public UserInterceptor(JwtProperties p){
        this.prop = p;
    }
    private static final ThreadLocal<UserInfo> tl = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        从cookies中拿到token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try{
//            解析token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
//            传递user
//            request.setAttribute("user", userInfo);
            tl.set(userInfo);
            return true;
        }catch (Exception e){
            log.error("[购物车服务] 解析身份失败");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        用完一定要删除
        tl.remove();
    }

    public static UserInfo getThreadLocal(){
        return tl.get();
    }
}
