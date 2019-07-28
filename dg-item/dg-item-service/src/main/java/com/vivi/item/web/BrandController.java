package com.vivi.item.web;

import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.Brand;
import com.vivi.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
//    查询表单
    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows",defaultValue = "5")Integer rows,
            @RequestParam(value = "sortBy",required = false)String sortBy,
            @RequestParam(value = "desc",defaultValue = "false")Boolean desc,
            @RequestParam(value = "key",required = false)String key
    ){
        return ResponseEntity.ok(brandService.queryBrandByPage(page,rows,sortBy,desc,key));
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam("categories") List<Long> cids) {
//    public ResponseEntity<Void> saveBrand(
//            @RequestParam("name")String name,
//            @RequestParam(value ="image",required=false)String url,
//            @RequestParam(value = "letter")Character letter,
//            @RequestParam(value = "categories",required=false)List<Long> cids){
//        Brand brand = new Brand();
//        brand.setLetter(letter);
//        brand.setName(name);
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
//    根据分类查品牌
    @GetMapping("cid/{id}")
    public ResponseEntity<List<Brand>> queryBrandsByCName(@PathVariable("id")Long cid){

        return ResponseEntity.ok(brandService.queryBrandByCName(cid));
    }
//    根据品牌id查品牌
    @GetMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id")Long id){
        return ResponseEntity.ok(brandService.queryBrandById(id));
    }

}
