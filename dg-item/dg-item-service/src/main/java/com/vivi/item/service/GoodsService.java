package com.vivi.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.vivi.common.vo.PageResult;
import com.vivi.item.mapper.*;
import com.vivi.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

//    public PageResult<SpuBo> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key,String sortBy,Boolean desc) {
//        // 1、查询SPU
//        // 分页,最多允许查100条
//        PageHelper.startPage(page, Math.min(rows, 100));
//        // 创建查询条件
//        Example example = new Example(Spu.class);
//        Example.Criteria criteria = example.createCriteria();
//        // 是否过滤上下架
//        if (saleable != false) {
//            criteria.orEqualTo("saleable", saleable);
//        }
//        // 是否模糊查询
//        if (StringUtils.isNotBlank(key)) {
//            criteria.andLike("title", "%" + key + "%");
//        }
////        排序
//        if (StringUtils.isNotBlank(sortBy)){
//            example.setOrderByClause(sortBy+(desc ? " DESC":" ASC"));
//        }
//        Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);
//
//        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
//            // 2、把spu变为 spuBo
//            SpuBo spuBo = new SpuBo();
//            // 属性拷贝
//            BeanUtils.copyProperties(spu, spuBo);
//
//            // 3、查询spu的商品分类名称,要查三级分类
//            List<String> names = this.categoryService.queryNameByIds(
//                    Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
//            // 将分类名称拼接后存入
//            spuBo.setCname(StringUtils.join(names, "/"));
//
//            // 4、查询spu的品牌名称
//            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
//            System.out.print(brand.toString()+"/");
//            spuBo.setBname(brand.getName());
//            System.out.print(spuBo.toString());
//            return spuBo;
//        }).collect(Collectors.toList());
//
//        return new PageResult<>(pageInfo.getTotal(), list);
//    }
    public PageResult<SpuBo> querySpuByPageAndSort(Integer page, Integer rows, Boolean saleable, String key,String sortBy,Boolean desc) {
//
        //1.查询spu，分页查询，最多查询100条
        PageHelper.startPage(page,Math.min(rows,100));

        //2.创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //3.条件过滤
        //3.1 是否过滤上下架
        if (saleable != null){
            criteria.orEqualTo("saleable",saleable);

        }
        //3.2 是否模糊查询
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //3.3 是否排序
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + (desc? " DESC":" ASC"));
        }
        Page<Spu> pageInfo = (Page<Spu>) this.spuMapper.selectByExample(example);


        //将spu变为spubo
        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            //1.属性拷贝
            BeanUtils.copyProperties(spu,spuBo);

            //2.查询spu的商品分类名称，各级分类
            List<String> nameList = this.categoryService.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
            //3.拼接名字,并存入
            spuBo.setCname(StringUtils.join(nameList,"/"));
            //4.查询品牌名称
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            System.out.print(brand.getName());
            spuBo.setBname(brand.getName());
            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(),list);
    }

//    保存新增商品
    @Transactional
    public void save(SpuBo spu) {
        // 保存spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insert(spu);
        // 保存spu详情
        spu.getSpuDetail().setSpuId(spu.getId());
        this.spuDetailMapper.insert(spu.getSpuDetail());

        // 保存sku和库存信息
        saveSkuAndStock(spu.getSkus(), spu.getId());
    }

    private void saveSkuAndStock(List<Sku> skus, Long id) {
        for (Sku sku : skus) {
            if (!sku.getEnable()) {
                continue;
            }
            // 保存sku
            sku.setSpuId(id);
            // 初始化时间
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetailById(Long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuBySpuId(Long spuId) {
        // 查询sku
        Sku record = new Sku();
        record.setSpuId(spuId);
        List<Sku> skus = this.skuMapper.select(record);
        for (Sku sku : skus) {
            // 同时查询出库存
            sku.setStock(this.stockMapper.selectByPrimaryKey(sku.getId()).getStock());
        }
        return skus;
    }
}
