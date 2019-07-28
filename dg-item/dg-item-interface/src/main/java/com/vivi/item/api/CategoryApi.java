package com.vivi.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface CategoryApi {
    @GetMapping("category/names")
    List<String> queryCategoryListByPids(@RequestParam("ids")List<Long> ids);
}
