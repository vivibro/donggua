package com.vivi.page.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vivi.common.utils.JsonUtils;
import com.vivi.page.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("item")
public class PageController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("{id}.html")
    public String toItemPage(@PathVariable("id") long spuId, Model model){
//        准备模型数据
        Map<String, Object> map = this.goodsService.loadModel(spuId);
//        将数据放入model
        model.addAllAttributes(map);
//        返回视图
        return "item";
    }
}
