package com.vivi.order.web;

import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.utils.JsonUtils;
import com.vivi.order.pojo.Order;
import com.vivi.order.pojo.OrderID;
import com.vivi.order.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Arrays;
import java.util.List;


@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单
     * @param order
     * @return 返回订单id
     */

    @PostMapping
    public ResponseEntity<OrderID> createOrder(@RequestBody Order order){
//        库存查询
        List<Long> skuId = this.orderService.queryStock(order);
        if (skuId.size() != 0){
            //库存不足
            throw new com.vivi.common.advice.exception.DgException(ExceptionEnum.GOOD_STOCK_IS_NULL);
        }
//        保存订单并返回订单号
        OrderID orderID = new OrderID();
        orderID.setOrderID(orderService.createOrder(order));

//        return ResponseEntity.ok(id);
        return new ResponseEntity<>(orderID, HttpStatus.CREATED);
    }
//    创建微信支付url
    @GetMapping("/url/{id}")
    public ResponseEntity<String> createPayUrl(@PathVariable("id") Long id){

        String WXPayUrl= orderService.createPayUrl(id);
        return ResponseEntity.ok(WXPayUrl);
    }
    @GetMapping("/status/{id}")
    public ResponseEntity<Integer> queryOrderStatus(@PathVariable("id") Long id) {

        return ResponseEntity.ok(orderService.queryOrderStatus(id).getCode());
    }

}
