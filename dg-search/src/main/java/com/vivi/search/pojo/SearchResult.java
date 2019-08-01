package com.vivi.search.pojo;

import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.Brand;
import com.vivi.item.pojo.Category;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
//除了返回分页信息以外，还需要聚合后的信息用于过滤筛选
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;

    private List<Brand> brands;
    private List<Map<String,Object>> specs; // 规格参数过滤条件

    public SearchResult(Long total, Integer totalPage, List<Goods> items, List<Category> categories,
                        List<Brand> brands,List<Map<String,Object>>specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}