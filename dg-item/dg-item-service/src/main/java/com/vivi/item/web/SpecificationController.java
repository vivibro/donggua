package com.vivi.item.web;

import com.vivi.item.pojo.Specification;
import com.vivi.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;
//    拦截查询
    @GetMapping("{id}")
    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id){
        Specification spec = this.specificationService.queryById(id);
        if (spec == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(spec.getSpecifications());
    }
//    拦截新增
    @PostMapping
    public ResponseEntity<Void> insertNewspecification(@RequestParam Long categoryId,@RequestParam String specifications){
        specificationService.insertNewspecificationByExmp(categoryId,specifications);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

//    拦截put请求的更新
    @PutMapping
    public ResponseEntity<Void> updateSpecification(@RequestParam Long categoryId,@RequestParam String specifications){
        specificationService.updateSpecificationByExmp(categoryId,specifications);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}