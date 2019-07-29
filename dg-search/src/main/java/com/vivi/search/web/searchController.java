package com.vivi.search.web;

import com.vivi.common.vo.PageResult;
import com.vivi.search.pojo.Goods;
import com.vivi.search.pojo.SearchRequest;
import com.vivi.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class searchController {

    @Autowired
    SearchService searchService;
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        return ResponseEntity.ok(searchService.search(request));
    }
}
