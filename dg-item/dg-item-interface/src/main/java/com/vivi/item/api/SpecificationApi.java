package com.vivi.item.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface SpecificationApi {
    @GetMapping("{id}")
    public String querySpecificationByCategoryId(@PathVariable("id") Long id);
}
