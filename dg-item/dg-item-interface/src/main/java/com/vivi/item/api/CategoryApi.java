package com.vivi.item.api;

import com.vivi.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    @GetMapping("category/names")
    List<String> queryCategoryListByPids(@RequestParam("ids")List<Long> ids);
    @GetMapping("category/namesRC")
    List<Category> queryCategoryListByPidsRC(@RequestParam("ids")List<Long> ids);
}
