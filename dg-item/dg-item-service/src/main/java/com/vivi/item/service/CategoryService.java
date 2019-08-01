package com.vivi.item.service;

import com.vivi.common.advice.exception.DgException;
import com.vivi.common.enums.ExceptionEnum;
import com.vivi.item.mapper.CategoryMapper;
import com.vivi.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryListByPid(Long pid) {


        Category t = new Category();
        t.setParentId(pid);
//        new一个对象，将对象中的部分参数赋值，把非空元素当做查询条件
        List<Category> categories = categoryMapper.select(t);
        if(CollectionUtils.isEmpty(categories)){
            throw new DgException(ExceptionEnum.CATEGORY_NOT_FIND);
        }
        return categories;
    }

    public List<String> queryNameByIds(List<Long> ids) {
        return this.categoryMapper.selectByIdList(ids).stream().map(Category::getName).collect(Collectors.toList());
    }
    public List<Category> queryNameByIdsRC(List<Long> ids){
        return this.categoryMapper.selectByIdList(ids);
    }
}
