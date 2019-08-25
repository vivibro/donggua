package com.vivi.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SmsDemoTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void sendSms() {
        HashMap<String, String> msg = new HashMap<>();
        msg.put("phone", "18380117701");
        msg.put("code","520520");
        amqpTemplate.convertAndSend("dg.sms.exchange","sms.verify.dode",msg);
    }
}