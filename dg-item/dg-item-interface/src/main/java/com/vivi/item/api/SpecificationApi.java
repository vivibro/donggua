package com.vivi.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("spec")
public interface SpecificationApi {
    @GetMapping("{id}")
    public String querySpecificationByCategoryId(@PathVariable("id") Long id);
}
