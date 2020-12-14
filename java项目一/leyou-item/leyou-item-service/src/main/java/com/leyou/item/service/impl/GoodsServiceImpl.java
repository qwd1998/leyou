package com.leyou.item.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.*;
import com.leyou.item.service.CategoryService;
import com.leyou.item.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 分页查询spu信息
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @Override
    public PageResult<SpuBo> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        //根据搜索条件查询
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }

        //判断是否上架或者下架
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        //设置分页小助手
        PageHelper.startPage(page, rows);

        //执行查询，获取spus集合
        List<Spu> spus = spuMapper.selectByExample(example);

        //完成分页查询
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);

        //将spu集合转换为SpuBo集合
        List<SpuBo> spuBos = spus.stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            // copy共同属性的值到新的对象
            BeanUtils.copyProperties(spu,spuBo);

            //查询分类名称
            List<String> names = categoryService.queryNameByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            spuBo.setCname(StringUtils.join(names,"/"));

            //查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());

            return spuBo;
        }).collect(Collectors.toList());

        //返回PageResult<SpuBos>
        return new PageResult<>(pageInfo.getTotal(),spuBos);
    }



    /**
     * 新增商品
     * @param spuBo
     */
    @Transactional
    @Override
    public void saveGoods(SpuBo spuBo) {

        //1.新增spu
        spuBo.setId(null);
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());
        spuMapper.insert(spuBo);

        //2.新增spuDetail
        SpuDetail spuDetail = spuBo.getSpuDetail();
        spuDetail.setSpuId(spuBo.getId());
        spuDetailMapper.insert(spuDetail);

         // 保存sku和stock信息
        saveSkuAndStock(spuBo);

        SendMsg("insert",spuBo.getId());
    }


    private void SendMsg(String type,Long supId) {
        try {
            amqpTemplate.convertAndSend("item."+type,supId);
        } catch (AmqpException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存sku和stock信息
     * @param spuBo
     */
    private void saveSkuAndStock(SpuBo spuBo){
        List<Sku> skus = spuBo.getSkus();
        for (Sku sku : skus) {
            //3.新增sku
            sku.setId(null);
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());

            skuMapper.insert(sku);

            //4.新增stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockMapper.insert(stock);
        }
    }


    /**
     * 根据spu_id查询spu_detail
     * @param spuId
     * @return
     */
    @Override
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = new SpuDetail();
        spuDetail.setSpuId(spuId);
        return spuDetailMapper.selectByPrimaryKey(spuDetail);
    }


    /**
     * 根据spuId查询sku的集合，并且从Stock表中查出库存，并加入sku对象中
     * @param spuId
     * @return
     */
    @Override
    public List<Sku> querySkusBySpuId(Long spuId) {
        //根据spuId从sku表中查询出sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuMapper.select(sku);

        //添加每个sku的库存
        skus.forEach(sku1 -> {
            //根据sku_id从stock表中查出Stock对象
            Stock stock = stockMapper.selectByPrimaryKey(sku1.getId());
            //把Stock对象中的stock存入sku中
            sku1.setStock(stock.getStock());
        });

        return skus;
    }

    /**
     * 修改商品
     *先查询skus的信息，再删除skus和stock信息，添加新的skus信息，再更新spu信息，再更新spuDetail信息
     * @param spuBo
     */
    @Transactional
    @Override
    public void updateGoods(SpuBo spuBo) {
        //1.先在sku表中查询所有的sku是否存在
        Sku sku = new Sku();
        sku.setSpuId(spuBo.getId());
        List<Sku> skus = skuMapper.select(sku);

        Stock stock = new Stock();
        //2.再判断skus是否存在，存在则删除全部，重新保存
        if (!CollectionUtils.isEmpty(skus)){
            for (Sku sku1 : skus) {
                //删除每个sku的库存
                stock.setSkuId(sku1.getId());
                stockMapper.delete(stock);

                skuMapper.delete(sku1); //删除sku1
            }
        }

        //3.再保存sku和库存
        saveSkuAndStock(spuBo);

        //4.更新spu信息
        spuBo.setLastUpdateTime(new Date());
        spuBo.setCreateTime(null);
        spuBo.setValid(null);
        spuBo.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spuBo);

        //5.更新spuDetail
        spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        SendMsg("update",spuBo.getId());
    }

    /**
     *根据id更新spu的信息
     * @param id
     * @return
     */
    @Transactional
    @Override
    public Spu updateSpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        spu.setValid(false);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setSaleable(null);
        spuMapper.updateByPrimaryKeySelective(spu);
        return spu;
    }

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @Override
    public Spu querySpuById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 根据skuId查询sku对象
     * @param skuId
     * @return
     */
    @Override
    public Sku querySkuBySkuId(Long skuId) {
        return  skuMapper.selectByPrimaryKey(skuId);
    }
}
