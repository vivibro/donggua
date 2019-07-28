package com.vivi.item.web;

import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.Sku;
import com.vivi.item.pojo.SpuBo;
import com.vivi.item.pojo.SpuDetail;
import com.vivi.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询SPU
     * @param page
     * @param rows
     * @param key
     * @return
     */
//    获取商品清单列表
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable",defaultValue = "true") Boolean saleable,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc){
        System.out.print("1");
        // 分页查询spu信息
        PageResult<SpuBo> result = this.goodsService.querySpuByPageAndSort(page, rows,saleable, key,sortBy,desc);
        if (result == null || result.getItems().size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result);
    }



    //    查询SpuDetail
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailById(@PathVariable("id") Long id) {
        SpuDetail detail = this.goodsService.querySpuDetailById(id);
        if (detail == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(detail);
    }
//    查询sku
    @GetMapping("/sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
      List<Sku> skus = this.goodsService.querySkuBySpuId(id);
      if (skus == null || skus.size() == 0) {
          return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
     return ResponseEntity.ok(skus);
    }
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo) {
        try {
            this.goodsService.save(spuBo);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}