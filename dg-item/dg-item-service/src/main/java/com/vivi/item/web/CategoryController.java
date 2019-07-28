package com.vivi.item.web;

import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.item.pojo.Category;
import com.vivi.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid")Long pid){

//        return ResponseEntity.status(HttpStatus.OK).body(null);
        return ResponseEntity.ok(categoryService.queryCategoryListByPid(pid));
    }

//    查询品牌列表
    @GetMapping("names")
    public ResponseEntity<List<String>> queryCategoryListByPids(@RequestParam("ids")List<Long> ids){
        List<String> strings = categoryService.queryNameByIds(ids);
        if(CollectionUtils.isEmpty(strings)){
            throw new DgException(ExceptionEnum.CATEGORY_NOT_FIND);
        }
        return ResponseEntity.ok(categoryService.queryNameByIds(ids));
    }
}
