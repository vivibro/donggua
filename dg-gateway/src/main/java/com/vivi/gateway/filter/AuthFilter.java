package com.vivi.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.vivi.common.utils.CookieUtils;
import com.vivi.entity.UserInfo;
import com.vivi.gateway.config.JwtProperties;
import com.vivi.gateway.config.filterProperties;
import com.vivi.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 拦截登录状态
 */

@Component
@EnableConfigurationProperties({JwtProperties.class,filterProperties.class})
//@EnableConfigurationProperties()
public class AuthFilter extends ZuulFilter {

//    继承Zuul, 实现4个方法

//    过滤器类型
    @Override
    public String filterType() {
        return "pre";
    }

//    过滤器顺序，放在内置过滤器之前
    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER -1;
    }
    //是否启用
    @Override
    public boolean shouldFilter() {
        String requestURL = ctx.getRequest().getRequestURI();
        return isUnAllowPath(requestURL);
    }

//    判断白名单
    public boolean isUnAllowPath(String url){
        boolean flag = true;
        for (String allow:fprop.getAllowPaths()) {
            if(url.startsWith(allow)){
                return false;
            }
        }
        return flag;
    }
    @Autowired
    private JwtProperties prop;
    @Autowired
    private filterProperties fprop;

    private RequestContext ctx = RequestContext.getCurrentContext();

    @Override
    public Object run() throws ZuulException {
        //获取上下文
//        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Cookie[] cookies = request.getCookies();
        try{
//            通过公钥解析token
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            UserInfo user = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
//            成功、校验权限

        }catch (Exception e){
//            解析失败,未登录，拦截
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
