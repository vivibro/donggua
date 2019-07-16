package com.vivi.item.mapper;

import com.vivi.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category> ,SelectByIdListMapper<Category, Long> {
}
