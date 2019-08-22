package com.vivi.sms.mq;

import com.vivi.common.utils.JsonUtils;
import com.vivi.sms.config.SmsProperties;
import com.vivi.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "sms:phone:";
    /**
     * 发短信验证码
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "dg.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verify.dode"
    ))
    public void listen(Map<String,String> msg){
        String phone = msg.remove("phone");
        //短信限流
        if (redisTemplate.opsForValue().get(KEY_PREFIX + phone).isEmpty()){
            return;
        }
        if(msg ==null){
            return;
        }
        if (StringUtils.isBlank(phone)){
            return;
        }


        smsUtils.sendSms(phone,prop.getSignName(),prop.getVerifyCodeTemplate(),JsonUtils.serialize(msg));

        //发送短信成功之后，把号码写入redis，并设置1分钟时长
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, String.valueOf(System.currentTimeMillis()),60, TimeUnit.MINUTES);
        //发送短信日志
        log.info("[短信服务]，发送短信验证码，手机号：{}",phone);

    }
}
