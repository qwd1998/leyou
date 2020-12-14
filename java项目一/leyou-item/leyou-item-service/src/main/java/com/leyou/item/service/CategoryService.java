package com.leyou.item.service;

import com.leyou.item.pojo.Category;

import java.util.List;

public interface CategoryService {

    /**
     * 根据父节点查询子节点信息
     * @param pid
     * @return
     */
    List<Category> queryCategoriesByPid(Long pid);

    List<String> queryNameByIds(List<Long> ids);
}
