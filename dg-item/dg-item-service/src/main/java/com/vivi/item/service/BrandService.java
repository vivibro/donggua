package com.vivi.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.common.vo.PageResult;
import com.vivi.item.mapper.BrandMapper;
import com.vivi.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //分页 PageHelper会自动拦截下面的查询，必须写在查询之前
        PageHelper.startPage(page, rows);
        //过滤
        /*
        WHERE 'name' like '%x%' or 'letter' ==x
        ORDER BY id DESC
         */
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name", "%" + key + "%").orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)){
//            记住这里的空格
            String orderByClasuse = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClasuse);
        }
        //查询
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)){
            throw new DgException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析list
        PageInfo<Brand> brandPageInfo = new PageInfo<>(brands);
        PageResult<Brand> result = new PageResult<Brand>(brandPageInfo.getTotal(),brands);
        return result;
    }

//    新增品牌的事物
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        this.brandMapper.insert(brand);
        for (Long cid : cids) {
            System.out.print(cid+" "+brand.getId());
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
//        //新增品牌
//        brand.setId(null);
//        int count = brandMapper.insert(brand);
//        if (count != 1){
//            throw new DgException(ExceptionEnum.BRAND_SAVE_ERROE);
//        }
//        //新增中间表的插入。
//        for (Long cid : cids) {
////            新增完之后会自动回显自增的bid
////            调用在mapper文件中手写的插入方法
//            count = brandMapper.insertCategoryBrand(cid, brand.getId());
//            if (count != 1){
//                throw new DgException(ExceptionEnum.BRAND_SAVE_ERROE);
//            }
//        }
    }

    public List<Brand> queryBrandByCName(Long cid) {
        return brandMapper.queryBrandByCId(cid);
    }
    public Brand queryBrandById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand==null){
            throw new DgException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }
}
