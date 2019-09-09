package com.vivi.page.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vivi.common.utils.JsonUtils;
import com.vivi.item.pojo.*;
import com.vivi.page.client.BrandClient;
import com.vivi.page.client.CategoryClient;
import com.vivi.page.client.GoodsClient;
import com.vivi.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

@Slf4j
@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(GoodsService.class);

    public Map<String, Object> loadModel(Long spuId){
//        try {
            // 查询spu
            Spu spu = this.goodsClient.querySpuById(spuId);

            // 查询spu详情
            SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spuId);
            // 查询sku
            List<Sku> skus = this.goodsClient.querySkuBySpuId(spuId);

            // 查询品牌
            List<Brand> brands = this.brandClient.queryBrandByIds(Arrays.asList(spu.getBrandId()));

            // 查询分类
            List<Category> categories = getCategories(spu);

//            // 查询组内参数
//            List<SpecGroup> specGroups = this.specificationClient.querySpecsByCid(spu.getCid3());
//
//            // 查询所有特有规格参数
//            List<SpecParam> specParams = this.specificationClient.querySpecParam(null, spu.getCid3(), null, false);
//            // 处理规格参数
//            Map<Long, String> paramMap = new HashMap<>();
//            specParams.forEach(param->{
//                paramMap.put(param.getId(), param.getName());
//            });
            String s = this.specificationClient.querySpecificationByCategoryId(spu.getCid3());
            List<Map<String,Object>> allSpecs = JsonUtils.nativeRead(s, new TypeReference<List<Map<String,Object>>>(){

            });
            Map<Integer,String> specialParamName = new HashMap<>();
            Map<Integer,String[]> specialParamValue = new HashMap<>();
            Map<Integer,String> specName = new HashMap<>();
            Map<Integer,Object> specValue = new HashMap<>();
//            将所有的属性名及属性值放入specName与specValue


            //获取sku特有规格参数
            String specTJson = spuDetail.getSpecTemplate();

            Map<String,String[]> specs = JsonUtils.nativeRead(specTJson, new TypeReference<Map<String, String[]>>() {
            });
            this.getAllSpecifications(allSpecs,specName,specValue);
            this.getSpecialSpec(specs, specName, specValue, specialParamName, specialParamValue);


            Map<Integer,String> name = new HashMap<>();
            Map<Integer,String[]> value = new HashMap<>();
            Integer num = 0;
            for (Map.Entry<String, String[]> entry : specs.entrySet()) {
                name.put(num, entry.getKey());
                value.put(num, entry.getValue());
                num++;
            }

            Map<Integer,String> csiname = new HashMap<>();
            Map<Integer,Object> csinvalue = new HashMap<>();
            String specificationsJson = spuDetail.getSpecifications();
            List<Map<String, Object>> specionsJson = JsonUtils.nativeRead(specificationsJson, new TypeReference<List<Map<String, Object>>>() {
            });
        getAllSpecifications(specionsJson,csiname,csinvalue);
//            for (Map<String,Object> spec :specionsJson){
//                List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
//                for (Map<String,Object> param :params){
//                    if (param.get("v") == null){
//                        continue;
//                    }
//                    csiname.put(num,param.get("k").toString());
//                    csinvalue.put(num, param.get("v").toString());
//                }
//            }
                //按照组构造规格参数
                List<Map<String, Object>> groups = this.getGroupsSpec(allSpecs, specName, specValue);
                Map<String, Object> map = new HashMap<>();
                map.put("spu", spu);
                map.put("spuDetail", spuDetail);
                map.put("skus", skus);
                map.put("brand", brands.get(0));
                map.put("categories", categories);
                map.put("specName", specName);
                map.put("specValue", specValue);
                map.put("groups", groups);
                map.put("specialParamName", specialParamName);
                map.put("specialParamValue", specialParamValue);
                map.put("csname", name);
                map.put("csvalue", value);
                map.put("csiname",csiname);
                map.put("csinvalue",csinvalue);

        return map;
//        } catch (Exception e) {
//            logger.error("加载商品数据出错,spuId:{}", spuId, e);
//        }
//        return null;
    }

    private List<Map<String, Object>> getGroupsSpec(List<Map<String, Object>> allSpecs, Map<Integer, String> specName, Map<Integer, Object> specValue) {
        List<Map<String, Object>> groups = new ArrayList<>();
        int i = 0;
        int j = 0;
        for (Map<String,Object> spec :allSpecs){
            List<Map<String, Object>> params = (List<Map<String, Object>>) spec.get("params");
            List<Map<String,Object>> temp = new ArrayList<>();
            for (Map<String,Object> param :params) {
                for (Map.Entry<Integer, String> entry : specName.entrySet()) {
                    if (entry.getValue().equals(param.get("k").toString())) {
                        String value = specValue.get(entry.getKey()) != null ? specValue.get(entry.getKey()).toString() : "无";
                        Map<String, Object> temp3 = new HashMap<>(16);
                        temp3.put("id", ++j);
                        temp3.put("name", entry.getValue());
                        temp3.put("value", value);
                        temp.add(temp3);
                    }
                }
            }
            Map<String,Object> temp2 = new HashMap<>(16);
            temp2.put("params",temp);
            temp2.put("id",++i);
            temp2.put("name",spec.get("group"));
            groups.add(temp2);
        }
        return groups;
    }

    private void getSpecialSpec(Map<String, String[]> specs, Map<Integer, String> specName, Map<Integer, Object> specValue, Map<Integer, String> specialParamName, Map<Integer, String[]> specialParamValue) {
        if (specs != null) {
            for (Map.Entry<String, String[]> entry : specs.entrySet()) {
                String key = entry.getKey();
                for (Map.Entry<Integer,String> e : specName.entrySet()) {
                    if (e.getValue().equals(key)){
                        specialParamName.put(e.getKey(),e.getValue());
                        //因为是放在数组里面，所以要先去除两个方括号，然后再以逗号分割成数组
                        String  s = specValue.get(e.getKey()).toString();
                        String result = StringUtils.substring(s,1,s.length()-1);
                        specialParamValue.put(e.getKey(), result.split(","));
                    }
                }
            }
        }
    }

    private void getAllSpecifications(List<Map<String, Object>> allSpecs, Map<Integer, String> specName, Map<Integer, Object> specValue) {
        String k = "k";
        String v = "v";
        String unit = "unit";
        String numerical = "numerical";
        String options ="options";
        int i = 0;
        if (allSpecs != null){
            for (Map<String,Object> s : allSpecs){
                List<Map<String, Object>> params = (List<Map<String, Object>>) s.get("params");
                for (Map<String,Object> param :params){
                    String result;
                    if (param.get(v) == null){
                        result = "无";
                    }else{
                        result = param.get(v).toString();
                    }
                    if (param.containsKey(numerical) && (boolean) param.get(numerical)) {
                        if (result.contains(".")){
                            Double d = Double.valueOf(result);
                            if (d.intValue() == d){
                                result = d.intValue()+"";
                            }
                        }
                        i++;
                        specName.put(i,param.get(k).toString());
                        specValue.put(i,result+param.get(unit).toString());
                    } else if (param.containsKey(options)){
                        i++;
                        specName.put(i,param.get(k).toString());
                        specValue.put(i,param.get(options));
                    }else {
                        i++;
                        specName.put(i,param.get(k).toString());
                        specValue.put(i,param.get(v));
                    }
                }
            }
        }
    }

    private List<Category> getCategories(Spu spu) {
        try {
            List<String> names = this.categoryClient.queryCategoryListByPids((
                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())));
            Category c1 = new Category();
            c1.setName(names.get(0));
            c1.setId(spu.getCid1());

            Category c2 = new Category();
            c2.setName(names.get(1));
            c2.setId(spu.getCid2());

            Category c3 = new Category();
            c3.setName(names.get(2));
            c3.setId(spu.getCid3());

            return Arrays.asList(c1, c2, c3);
        } catch (Exception e) {
            logger.error("查询商品分类出错，spuId：{}", spu.getId(), e);
        }
        return null;
    }





    /**
     * 修改模板引擎输出流，将页面输出为静态页面。
     * @param spuID
     */
    public void createHtml(Long spuID){

//        上下文
        Context context = new Context();
        context.setVariables(loadModel(spuID));
//        输出流
        PrintWriter printWriter = null;
        File dest= new File("/Users/vi-vibro/Documents/code",spuID+".html");
//        如果存在则先删除
        if (dest.exists()){
            dest.delete();
        }
        try{
        printWriter= new PrintWriter(dest, "UTF-8");
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常",e);
        };
        templateEngine.process("item", context, printWriter);
    }

    public void deleteHtml(Long spuID) {
        File dest= new File("/Users/vi-vibro/Documents/code",spuID+".html");
//        如果存在则删除
        if (dest.exists()){
            dest.delete();
        }
    }


//    public void asyncExcute(Long spuId) {
//        ThreadUtils.execute(()->createHtml(spuId));
//        /*ThreadUtils.execute(new Runnable() {
//            @Override
//            public void run() {
//                createHtml(spuId);
//            }
//        });*/
//    }
}