package com.vivi.page.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;
    @Test
    public void createHtml() {
        goodsService.createHtml(141L);
    }
}