package com.leyou.item.service.impl;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据
     * @param pid
     * @return
     */
    @Override
    public List<Category> queryCategoriesByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        return categoryMapper.select(category);
    }

    /**
     * 根据ids查询出分类的名称
     * @param ids
     * @return
     */
    @Override
    public List<String> queryNameByIds(List<Long> ids) {
        List<Category> categories = categoryMapper.selectByIdList(ids);
        return  categories.stream().map(category -> category.getName()).collect(Collectors.toList());
    }
}
