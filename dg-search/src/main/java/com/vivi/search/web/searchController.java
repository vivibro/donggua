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

import java.util.Map;

@RestController
public class searchController {

    @Autowired
    SearchService searchService;
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        for (Map.Entry<String, String> entry : request.getFilter().entrySet()) {
            System.out.print("key:"+entry.getKey()+"value:"+entry.getValue()+"ok");
        }
        return ResponseEntity.ok(searchService.search(request));
    }
}
