package com.vivi.item.api;

import com.vivi.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("brand")
public interface BrandApi {
    @GetMapping("/{id}")
    Brand queryBrandById(@PathVariable("id")Long id);

    @GetMapping("/brands")
    List<Brand> queryBrandByIds(@RequestParam("ids")List<Long> ids);
}
