package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();


        //根据name模糊查询或者根据首字母查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("name", "%" + key + "%").orEqualTo("letter", key);
        }

        //添加分页条件
        PageHelper.startPage(page, rows);

        //添加排序条件
        if (StringUtils.isNotBlank(sortBy)) {
            example.setOrderByClause(sortBy + " " + (desc ? "desc" : "asc"));
        }

        List<Brand> brands = brandMapper.selectByExample(example);

        //包装成PageInfo对象
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        //包装成分页结果集返回

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 添加品牌到分类中
     *
     * @param brand
     * @param cids
     */
    @Transactional
    @Override
    public void saveBrand(Brand brand, List<Long> cids) {
        //先保存brand表中的数据
        brandMapper.insertSelective(brand);

        for (Long cid : cids) {

            //再保存brand和category中间表
            brandMapper.saveBrandAndCategory(brand.getId(),cid);

        }
    }

    /**
     * 更新Brand和Category
     * @param brand
     * @param cids
     */
    @Transactional
    @Override
    public void updateBrand(Brand brand, List<Long> cids) {
        //先更新brand表中的数据
        brandMapper.updateByPrimaryKeySelective(brand);

        //再删除中间表的信息
        brandMapper.deleteBrandAndCategory(brand.getId());
        //再插入新的tb_brand_category中间表信息
        for (Long cid : cids) {
            brandMapper.saveBrandAndCategory(brand.getId(),cid);
        }

    }

    /**
     * 根据分类cid查询出所有的品牌
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        return brandMapper.queryBrandByCid(cid);
    }

    /**
     * 根据bid查询出所有的category
     * @param bid
     * @return
     */
    @Override
    public List<Category> queryCategoryByBid(Long bid) {
        return brandMapper.queryCategoryByBid(bid);
    }

    /**
     * 根据bid删除品牌
     * @param bid
     */
    @Transactional
    @Override
    public void deleteBrandByBid(Long bid) {
        //先删除tb_category_brand
        brandMapper.deleteBrandAndCategory(bid);

        // 再删除brand
        Brand brand = new Brand();
        brand.setId(bid);
        brandMapper.delete(brand);
    }

    /**
     * 根据bid查询品牌名称
     * @param bid
     * @return
     */
    @Override
    public Brand queryBrandByBid(Long bid) {
        return brandMapper.selectByPrimaryKey(bid);
    }

}
