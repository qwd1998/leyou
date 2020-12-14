package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class GoodsController {

    @Autowired
    private GoodsService goodsService;



    /**
     * 根据条件分页查询spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuBoByPage(
            @RequestParam(name="key",required = false)String key,
            @RequestParam(name="saleable",required = false)Boolean saleable,
            @RequestParam(name="page",defaultValue = "1")Integer page,
            @RequestParam(name="rows",defaultValue = "5")Integer rows
    ){
        PageResult<SpuBo> result = goodsService.querySpuBoByPage(key,saleable,page,rows);
        if (result == null || CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 新增商品
     * @param spuBo
     * @return
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spuBo){
        goodsService.saveGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据spu_id查询spu_detail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("spuId")Long spuId){
        if (spuId == null || spuId < 0){
            return ResponseEntity.badRequest().build();
        }
        SpuDetail spuDetail = goodsService.querySpuDetailBySpuId(spuId);
        if (spuDetail == null ){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spuDetail);
    }

    /**
     * 根据spu_id查询出所有的sku信息
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkusBySpuId(@RequestParam("id")Long spuId){
        if (spuId == null || spuId < 0){
            return ResponseEntity.badRequest().build();
        }
        List<Sku> skus = goodsService.querySkusBySpuId(spuId);
        if (skus == null || CollectionUtils.isEmpty(skus)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(skus);
    }

    /**
     * 根据spuId更新spu的信息
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> updateSpuById(@PathVariable("id")Long id){
        if (id == null || id < 0){
            return ResponseEntity.badRequest().build();
        }
        Spu spu = goodsService.updateSpuById(id);


        return ResponseEntity.ok(spu);
    }

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id")Long id){
        if (id == null || id < 0){
            return ResponseEntity.badRequest().build();
        }
        Spu spu = goodsService.querySpuById(id);
        if (spu == null ){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(spu);
    }


    /**
     * 根据skuId查询sku对象
     * @param skuId
     * @return
     */
    @GetMapping("sku/{skuId}")
    public ResponseEntity<Sku> querySkuBySkuId(@PathVariable("skuId")Long skuId){
        Sku sku = goodsService.querySkuBySkuId(skuId);
        if (sku == null ){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(sku);
    }
}
