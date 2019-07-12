package com.vivi.item.mapper;

import com.vivi.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;



public interface BrandMapper extends Mapper<Brand> {

//    手写一个sql语句用于插入中间表，用到Insert注解
    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) VALUES (#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid")Long cid,@Param("bid")Long bid);
}
