package com.vivi.cart.service;

import com.vivi.cart.interceptor.UserInterceptor;
import com.vivi.cart.pojo.Cart;
import com.vivi.common.utils.JsonUtils;
import com.vivi.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

public class CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id:";

    public void addCart(Cart cart) {
//        从线程域中取出userinfo
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        String key = KEY_PREFIX + userInfo.getId();
//        绑定key
        BoundHashOperations ops = redisTemplate.boundHashOps(key);

        String hashkey = cart.getSkuId().toString();
//        判断是否存在,存在增加数量，不存在新增
        if(ops.hasKey(hashkey)){
            String json = ops.get(hashkey).toString();
            Cart parse = JsonUtils.parse(json, Cart.class);
            parse.setNum(parse.getNum() + cart.getNum());
            json = JsonUtils.serialize(parse);
            ops.put(hashkey, json);
        }else {
            ops.put(hashkey, JsonUtils.serialize(cart));
        }
    }
}
