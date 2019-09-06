package com.vivi.order.web;


import com.vivi.order.service.OrderService;
import io.swagger.models.Xml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("notify")
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * 处理微信回调
     * @param payResult
     * @return 声明返回类型一定是xml
     */
    @PostMapping(value = "pay",produces = "application/xml")
    public Map<String,String> hello(@RequestBody Map<String,String> payResult ){

        orderService.handlerNotify(payResult);
        Map<String,String> result = new HashMap<>();
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        return result;
    }
}
