package com.vivi.item.api;

import com.vivi.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface BrandApi {
    @GetMapping("brand/{id}")
    Brand queryBrandById(@PathVariable("id")Long id);
}
