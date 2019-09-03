package com.vivi.item.api;

import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.Sku;
import com.vivi.item.pojo.Spu;
import com.vivi.item.pojo.SpuBo;
import com.vivi.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@RequestMapping("goods")
//在这里提供接口，不加入feign注解 feign客户端再继承此接口 加上feignClient注解
public interface GoodsApi {
    @GetMapping("/spu/detail/{id}")
    SpuDetail querySpuDetailById(@PathVariable("id") Long id);
    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);
    @GetMapping("/spu/page")
    PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable",defaultValue = "true") Boolean saleable);
    @GetMapping("spu/{id}")
    Spu querySpuById(@RequestParam("id") Long id);
    @GetMapping("{id}")
    Sku querySkuById(@PathVariable("id")Long id);
}
