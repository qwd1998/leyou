package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {




    @Insert("insert into tb_category_brand(category_id,brand_id) values(#{cid},#{id})")
    void saveBrandAndCategory(@Param("id") Long id, @Param("cid") Long cid);

    @Select("SELECT *FROM tb_brand WHERE id IN (SELECT brand_id FROM tb_category_brand WHERE category_id = #{cid}) ")
    List<Brand> queryBrandByCid(Long cid);

    @Delete("DELETE  FROM tb_category_brand  WHERE brand_id=#{bid}")
    void deleteBrandAndCategory(@Param("bid") Long bid);

    @Select("SELECT * FROM tb_category WHERE id IN (SELECT category_id FROM tb_category_brand WHERE brand_id = #{bid})")
    List<Category> queryCategoryByBid(Long bid);

}
