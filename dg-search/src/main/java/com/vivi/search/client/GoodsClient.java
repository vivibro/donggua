package com.vivi.search.client;

import com.vivi.common.vo.PageResult;
import com.vivi.item.api.GoodsApi;
import com.vivi.item.pojo.Sku;
import com.vivi.item.pojo.SpuBo;
import com.vivi.item.pojo.SpuDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {
}
