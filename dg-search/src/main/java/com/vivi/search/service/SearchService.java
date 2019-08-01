package com.vivi.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivi.common.utils.JsonUtils;
import com.vivi.common.utils.NumberUtils;
import com.vivi.common.vo.PageResult;
import com.vivi.item.pojo.*;
import com.vivi.search.client.BrandClient;
import com.vivi.search.client.CategoryClient;
import com.vivi.search.client.GoodsClient;
import com.vivi.search.client.SpecificationClient;
import com.vivi.search.pojo.Goods;
import com.vivi.search.pojo.SearchRequest;
import com.vivi.search.pojo.SearchResult;
import com.vivi.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    
    @Autowired
    private ElasticsearchTemplate template;

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
        QueryBuilder bascQuery =QueryBuilders.matchQuery("all",key);
        queryBuilder.withQuery(bascQuery);
//        过滤返回结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        
//        聚合分类和品牌的信息
//        聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
//        聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


//        查询
//        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
//        解析结果
        long total = result.getTotalElements();
        int totalPages = result.getTotalPages();
        List<Goods> content = result.getContent();
        
//        解析聚合结果
        Aggregations aggs = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggs.get(categoryAggName));
        System.out.print(categories.toString());
        List<Brand> brands = parseBrandAgg(aggs.get(brandAggName));
//        如果搜索结果仅一个分类，才进行聚合更详细的参数
        List<Map<String,Object>> specs = new ArrayList<>();

//        if (categories != null&&categories.size()==1){
            specs = getSpec(categories.get(0).getId(),bascQuery);
//        }
        SearchResult searchResult = new SearchResult(total, totalPages, content, categories, brands,specs);
        return searchResult;
    }

//    聚合查询品牌
    private List<Brand> parseBrandAgg(LongTerms aggregation) {
        List<Long> ids = aggregation.getBuckets().stream()
                .map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        return brandClient.queryBrandByIds(ids);
    }
//    聚合查询分类
    private List<Category> parseCategoryAgg(LongTerms aggregation) {
        List<Long> ids = aggregation.getBuckets()
                .stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        return categoryClient.queryCategoryListByPidsRC(ids);
    }

//    通过分类获取属性字段
    private List<Map<String,Object>> getSpec(Long cid,QueryBuilder query){
        List<Map<String,Object>> specs = new ArrayList<>();
//        1查询需要进行聚合的参数
        String Json = this.specificationClient.querySpecificationByCategoryId(cid);
//       1.1 反序列化
        specs = JsonUtils.nativeRead(Json, new TypeReference<List<Map<String, Object>>>(){});
//        1.2
//         //2.过滤出可以搜索的规格参数名称，分成数值类型、字符串类型
        Set<String> strSpec = new HashSet<>();
        //准备map，用来保存数值规格参数名及单位
        Map<String,String> numericalUnits = new HashMap<>();
        //解析规格
        String searchable = "searchable";
        String numerical = "numerical";
        String k = "k";
        String unit = "unit";
        for (Map<String,Object> spec :specs){
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            params.forEach(param ->{
                if ((boolean)param.get(searchable)){
                    if (param.containsKey(numerical) && (boolean)param.get(numerical)){
                        numericalUnits.put(param.get(k).toString(),param.get(unit).toString());
                    }else {
                        strSpec.add(param.get(k).toString());
                    }
                }
            });
        }
        //3.聚合计算数值类型的interval
        Map<String,Double> numericalInterval = getNumberInterval(cid,numericalUnits.keySet());
        return this.aggForSpec(strSpec,numericalInterval,numericalUnits,query);
    }

//    得到Interval
    private Map<String, Double> getNumberInterval(Long id, Set<String> keySet) {
        Map<String,Double> numbericalSpecs = new HashMap<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //不查询任何数据
        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",id.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
        //添加stats类型的聚合,同时返回avg、max、min、sum、count等
        for (String key : keySet){
//            索引存对象的时候回变成对象名.属性名
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }
//        查聚合结果
        Map<String,Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(),
                searchResponse -> searchResponse.getAggregations().asMap()
        );
        for (String key : keySet){
            InternalStats stats = (InternalStats) aggregationMap.get(key);
            double interval = this.getInterval(stats.getMin(),stats.getMax(),stats.getSum());
            numbericalSpecs.put(key,interval);
        }
        return numbericalSpecs;
    }

//    根据最小值，最大值，sum计算interval
    private double getInterval(double min, double max, Double sum) {
        //显示7个区间
        double interval = (max - min) / 6.0d;
        //判断是否是小数
        if (sum.intValue() == sum){
            //不是小数，要取整十、整百
            int length = StringUtils.substringBefore(String.valueOf(interval),".").length();
            double factor = Math.pow(10.0,length - 1);
            return Math.round(interval / factor)*factor;
        }else {
            //是小数的话就保留一位小数
            return NumberUtils.scale(interval,1);
        }
    }
//    根据规格参数，聚合得到过滤属性值
    private List<Map<String, Object>> aggForSpec(Set<String> strSpec, Map<String, Double> numericalInterval, Map<String, String> numericalUnits, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs = new ArrayList<>();
        //准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //聚合数值类型
        for (Map.Entry<String,Double> entry : numericalInterval.entrySet()) {
            queryBuilder.addAggregation(AggregationBuilders.histogram(entry.getKey()).field("specs." + entry.getKey()).interval(entry.getValue()).minDocCount(1));
        }
        //聚合字符串
        for (String key :strSpec){
            queryBuilder.addAggregation(AggregationBuilders.terms(key).field("specs."+key+".keyword"));
        }

        //解析聚合结果
        Map<String,Aggregation> aggregationMap = this.elasticsearchTemplate.query(queryBuilder.build(), SearchResponse:: getAggregations).asMap();

        //解析数值类型
        for (Map.Entry<String,Double> entry :numericalInterval.entrySet()){
            Map<String,Object> spec = new HashMap<>();
            String key = entry.getKey();
            spec.put("k",key);
            spec.put("unit",numericalUnits.get(key));
            //获取聚合结果
            InternalHistogram histogram = (InternalHistogram) aggregationMap.get(key);
            spec.put("options",histogram.getBuckets().stream().map(bucket -> {
                Double begin = (Double) bucket.getKey();
                Double end = begin + numericalInterval.get(key);
                //对begin和end取整
                if (NumberUtils.isInt(begin) && NumberUtils.isInt(end)){
                    //确实是整数，直接取整
                    return begin.intValue() + "-" + end.intValue();
                }else {
                    //小数，取2位小数
                    begin = NumberUtils.scale(begin,2);
                    end = NumberUtils.scale(end,2);
                    return begin + "-" + end;
                }
            }).collect(Collectors.toList()));
            specs.add(spec);
        }

        //解析字符串类型
        strSpec.forEach(key -> {
            Map<String,Object> spec = new HashMap<>();
            spec.put("k",key);
            StringTerms terms = (StringTerms) aggregationMap.get(key);
            spec.put("options",terms.getBuckets().stream().map((Function<StringTerms.Bucket, Object>) StringTerms.Bucket::getKeyAsString).collect(Collectors.toList()));
            specs.add(spec);
        });
        return specs;
    }
}
