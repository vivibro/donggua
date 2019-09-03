package com.vivi.cart.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivi.cart.client.GoodsClient;
import com.vivi.cart.interceptor.UserInterceptor;
import com.vivi.cart.pojo.Cart;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.utils.JsonUtils;
import com.vivi.entity.UserInfo;
import com.vivi.item.pojo.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private GoodsClient goodsClient;

    private static final String KEY_PREFIX = "cart:user:id:";

    public void addCart(Cart cart) {
//        从线程域中取出userinfo
        UserInfo userInfo = UserInterceptor.getThreadLocal();

        String key = KEY_PREFIX + userInfo.getId();
//        绑定key
        BoundHashOperations<String,Object,Object> ops = redisTemplate.boundHashOps(key);

        String hashkey = cart.getSkuId().toString();
//        判断是否存在,存在增加数量，不存在新增
        if(ops.hasKey(hashkey)){
            String json = ops.get(hashkey).toString();
            Cart parse = JsonUtils.parse(json, Cart.class);
            parse.setNum(parse.getNum() + cart.getNum());
            json = JsonUtils.serialize(parse);
            ops.put(hashkey, json);
        }else {
            Sku sku = this.goodsClient.querySkuById(cart.getSkuId());
            cart.setImage(sku.getImages());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            ops.put(hashkey, JsonUtils.serialize(cart));
        }
    }

    public List<Cart> queryCartList() {
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        String key = KEY_PREFIX + userInfo.getId();
        if(!this.redisTemplate.hasKey(key)){
            // 不存在，直接返回
            return null;
//            throw new DgException(ExceptionEnum.CART_NOT_FOUND);
        }
        BoundHashOperations<String,Object,Object> ops = redisTemplate.boundHashOps(key);
        List<Cart> carts = ops.values().stream().map(
                o -> JsonUtils.parse(o.toString(),Cart.class)).collect(Collectors.toList());
        return carts;
    }

    public void updateCartList(Cart cart) {
        //        从线程域中取出userinfo
        UserInfo userInfo = UserInterceptor.getThreadLocal();
        String key = KEY_PREFIX + userInfo.getId();
//        绑定key
        BoundHashOperations<String,Object,Object> ops = redisTemplate.boundHashOps(key);
        ops.put(cart.getSkuId().toString(), cart.getNum());
    }

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        // 设置value的序列化规则和 key的序列化规则
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
