package com.vivi.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivi.common.utils.JsonUtils;
import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.Brand;
import com.vivi.item.pojo.Sku;
import com.vivi.item.pojo.Spu;
import com.vivi.item.pojo.SpuDetail;
import com.vivi.search.client.BrandClient;
import com.vivi.search.client.CategoryClient;
import com.vivi.search.client.GoodsClient;
import com.vivi.search.client.SpecificationClient;
import com.vivi.search.pojo.Goods;
import com.vivi.search.pojo.SearchRequest;
import com.vivi.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class SearchService {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsRepository goodsRepository;

    private ObjectMapper mapper = new ObjectMapper();

//导入索引数据
    public Goods buildGoods(Spu spu)throws IOException  {
        Goods Good = new Goods();

        //1.查询商品分类名称
        List<String> categoryNames = this.categoryClient.queryCategoryListByPids(
                Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
        //2. 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //2.查询sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        //3.查询详情
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spu.getId());

        //4.处理sku,仅封装id，价格、标题、图片、并获得价格集合
        List<Long> prices = new ArrayList<>();
        List<Map<String,Object>> skuLists = new ArrayList<>();
        skus.forEach(sku -> {
            prices.add(sku.getPrice());
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            //取第一张图片
            skuMap.put("image", StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            skuLists.add(skuMap);
        });

        //提取公共属性
        List<Map<String,Object>> genericSpecs = mapper.readValue(spuDetail.getSpecifications(),new TypeReference<List<Map<String,Object>>>(){});
        //提取特有属性
        Map<String,Object> specialSpecs = mapper.readValue(spuDetail.getSpecTemplate(),new TypeReference<Map<String,Object>>(){});

        //过滤规格模板，把所有可搜索的信息保存到Map中,key是规格参数的名字,value是规格参数的值
        Map<String,Object> specMap = new HashMap<>();

        String searchable = "searchable";
        String v = "v";
        String k = "k";
        String options = "options";

        genericSpecs.forEach(m -> {
            List<Map<String, Object>> params = (List<Map<String, Object>>) m.get("params");
            params.forEach(spe ->{
                if ((boolean)spe.get(searchable)){
                    if (spe.get(v) != null){
                        specMap.put(spe.get(k).toString(), spe.get(v));
                    }else if (spe.get(options) != null){
                        specMap.put(spe.get(k).toString(), spe.get(options));
                    }
                }
            });
        });
        Good.setSubTitle(spu.getSubTitle());
        Good.setId(spu.getId());
        Good.setBrandId(spu.getBrandId());
        Good.setCid1(spu.getCid1());
        Good.setCid2(spu.getCid2());
        Good.setCid3(spu.getCid3());
        Good.setCreateTime(spu.getCreateTime());
        //搜索字段,包含标题,分类,品牌,规格
        Good.setAll(spu.getTitle() + " " + StringUtils.join(categoryNames, " ") + " " + brand.getName());
        Good.setPrice(prices); //价格集合
        Good.setSkus(mapper.writeValueAsString(skuLists)); //所有sku的集合的json格式
        Good.setSpecs(specMap); //所有的可搜索的规格参数
        return Good;
    }

//    搜索
    public PageResult<Goods> search(SearchRequest request) {
        //        分页
//        elasticSearch初始页默认为0
        Integer page = request.getPage()-1;
        Integer size = request.getSize();
        //        过滤
        String key = request.getKey();

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(page, size));
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key));
//        过滤返回结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));

//        查询
        Page<Goods> result = goodsRepository.search(queryBuilder.build());
//        解析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> content = result.getContent();
        return new PageResult<Goods>(total, totalPages, content);
    }


//    private String chooseSegment(String value, SpecParam p) {
//        double val = NumberUtils.toDouble(value);
//        String result = "其它";
//        // 保存数值段
//        for (String segment : p.getSegments().split(",")) {
//            String[] segs = segment.split("-");
//            // 获取数值范围
//            double begin = NumberUtils.toDouble(segs[0]);
//            double end = Double.MAX_VALUE;
//            if(segs.length == 2){
//                end = NumberUtils.toDouble(segs[1]);
//            }
//            // 判断是否在范围内
//            if(val >= begin && val < end){
//                if(segs.length == 1){
//                    result = segs[0] + p.getUnit() + "以上";
//                }else if(begin == 0){
//                    result = segs[1] + p.getUnit() + "以下";
//                }else{
//                    result = segment + p.getUnit();
//                }
//                break;
//            }
//        }
//        return result;
//    }
}
