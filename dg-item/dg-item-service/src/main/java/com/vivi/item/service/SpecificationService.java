package com.vivi.item.service;

import com.vivi.item.mapper.SpecificationMapper;
import com.vivi.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecificationService {
    @Autowired
    private SpecificationMapper specificationMapper;
//    查询参数列表
    public Specification queryById(Long id) {
        return specificationMapper.selectByPrimaryKey(id);
    }

//    新增参数信息
    @Transactional
    public void insertNewspecificationByExmp(Long categoryId, String specifications) {
        Specification specification = new Specification();
        specification.setCategoryId(categoryId);
        specification.setSpecifications(specifications);
        specificationMapper.insert(specification);
    }
//    更新参数信息
    @Transactional
    public void updateSpecificationByExmp(Long categoryId, String specifications) {
        Specification specification = new Specification();
        specification.setCategoryId(categoryId);
        specification.setSpecifications(specifications);
        specificationMapper.updateByPrimaryKey(specification);
    }
}
