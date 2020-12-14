package com.leyou.goods.service;

import com.leyou.goods.client.BrandClient;
import com.leyou.goods.client.CategoryClient;
import com.leyou.goods.client.GoodsClient;
import com.leyou.goods.client.SpecificationClient;
import com.leyou.item.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class GoodsService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> model = new HashMap<>();

        //根据supId查询sup
        Spu spu = goodsClient.querySpuById(spuId);

        //根据supId查询supDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);

        //根据分类cid查询分类名称
        List<Long> ids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = categoryClient.queryNamesByIds(ids);
        //初始化categories  List<Map<Long,String>>
        List<Map<String,Object>> categories = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Map<String,Object> map = new HashMap<>();
            map.put("name",names.get(i));
            map.put("id",ids.get(i));
            categories.add(map);
        }

        //根据brandId查询brand
        Brand brand = brandClient.queryBrandByBid(spu.getBrandId());


        //根据spuId查询skus集合
        List<Sku> skus = goodsClient.querySkusBySpuId(spu.getId());

        //根据分组id查询groups集合  Map<Long,String>
        List<SpecGroup> groups = specificationClient.queryGroupAndParamByCid(spu.getCid3());

        //查询所有的特殊规格参数
        List<SpecParam> params = specificationClient.queryParamByGid(null, spu.getCid3(), false, null);
        //初始化特殊规格参数的map
        Map<Long, String> paramMap = new HashMap<>();
        params.forEach(param->{
            paramMap.put(param.getId(),param.getName());
        });

        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categories);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);

        return model;
    }
}
