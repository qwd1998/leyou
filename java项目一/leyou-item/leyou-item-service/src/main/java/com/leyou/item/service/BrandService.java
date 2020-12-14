package com.leyou.item.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;

import java.util.List;

public interface BrandService {

    /**
     * 根据查询条件分页查询品牌信息
     * @param key  查询条件
     * @param page  页面
     * @param rows  条数
     * @param sortBy  字段排序
     * @param desc  降序
     * @return
     */

    PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc);


    void saveBrand(Brand brand, List<Long> cids);


    void updateBrand(Brand brand, List<Long> cids);

    List<Brand> queryBrandByCid(Long cid);

    List<Category> queryCategoryByBid(Long bid);

    void deleteBrandByBid(Long bid);

    Brand queryBrandByBid(Long bid);
}
