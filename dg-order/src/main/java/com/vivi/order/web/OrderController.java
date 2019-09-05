package com.vivi.order.web;

import com.vivi.order.pojo.Order;
import com.vivi.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

//    返回订单id
    @PostMapping
    public ResponseEntity<List<Long>> createOrder(@RequestBody Order order){
//        库存查询
        List<Long> skuId = this.orderService.queryStock(order);
        if (skuId.size() != 0){
            //库存不足
            return new ResponseEntity<>(skuId,HttpStatus.OK);
        }
//        保存订单并返回订单号
        Long id = orderService.createOrder(order);
        return new ResponseEntity<>(Arrays.asList(id), HttpStatus.CREATED);
    }
    @GetMapping("/url/{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long id){
        Order order = orderService.queryOrderById(id);
        return ResponseEntity
    }
}
