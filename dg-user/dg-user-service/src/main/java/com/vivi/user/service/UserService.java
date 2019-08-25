package com.vivi.user.service;

import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.user.mapper.UserMapper;
import com.vivi.user.pojo.User;
import com.vivi.user.utils.CodecUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;


@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public boolean checkData(String data, Integer type) {
//        判断类型
        User user = new User();
        if (type == 1){
            user.setUsername(data);
        }else if (type == 2){
            user.setPhone(data);
        }else throw new DgException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        int count = userMapper.selectCount(user);
        return count == 0;
    }

    public void sendCode(String phone) {
        HashMap<String, String> msg = new HashMap<>();
        Integer c = (int) ((Math.random() * 9 + 1) * 100000);
        String code = String.valueOf(c);
        msg.put("phone", phone);
        msg.put("code",code);
//        发送消息
        amqpTemplate.convertAndSend("dg.sms.exchange","sms.verify.dode",msg);
//        保存验证码到redis,设置code失效时间
        String key = KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

    }

    public void register(User user, String code) {
//        校验验证码
        if (!redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone()).equals(code)){
            throw new DgException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
//        密码加密  md5,引入commons-codec依赖 加入随机生成的盐(利用UUID去-生成)，再进行一次md5加密，这里将方法封装。
//        DigestUtils.md5Hex("fqbfo" + DigestUtils.md5Hex(user.getPassword()) + "fiwq");
        String salt = CodecUtils.generateSalt();
        String password = CodecUtils.md5Hex(user.getPassword(),salt);
//        写入数据库
        user.setPassword(password);
        user.setSalt(salt);
        user.setCreated(new Date(System.currentTimeMillis()));
        userMapper.insert(user);
    }

    public User queryUserByUNAndPW(String username, String password) {
//        用用户名去查密码，然后将明文密码通过md5与之对比，相同则返回
//        还有个原因是，数据库设计时候，将username添加了索引，如果将账号密码一起加入查询条件，会变得很慢
        User user = new User();
        user.setUsername(username);
        User one = userMapper.selectOne(user);
//        搜不到账号，或者密码错误
        if (one == null||!one.getPassword().equals(CodecUtils.md5Hex(password,one.getSalt()))){
            throw new DgException(ExceptionEnum.INVALID_USERNAME_OR_PASSWORD);
        }
        return one;
    }
}
