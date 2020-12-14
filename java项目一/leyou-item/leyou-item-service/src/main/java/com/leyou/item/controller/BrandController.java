package com.leyou.item.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * 根据查询条件分页查询品牌信息
     * @param key  查询条件
     * @param page  页面
     * @param rows  条数
     * @param sortBy  字段排序
     * @param desc  降序
     * @return
     */
    @GetMapping("/page")
    public ResponseEntity<PageResult<Brand>> queryBrandsByPage(
            @RequestParam(value="key",required = false)String key,
            @RequestParam(value="page",required = false)Integer page,
            @RequestParam(value="rows",required = false)Integer rows,
            @RequestParam(value="sortBy",required = false)String sortBy,
            @RequestParam(value="desc",required = false)Boolean desc){

        PageResult<Brand> result = this.brandService.queryBrandsByPage(key,page,rows,sortBy,desc);
        if (result==null || CollectionUtils.isEmpty(result.getItems())){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 添加品牌到分类中
     * @param brand
     * @param cids
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(Brand brand,@RequestParam(name = "cids")List<Long> cids){
        brandService.saveBrand(brand,cids);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改
     * @param brand
     * @param cids
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateBrand(Brand brand,@RequestParam(name = "cids",required = false)List<Long> cids){
        brandService.updateBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 根据分类cid查询出所有的品牌
     * @param cid
     * @return
     */
    @GetMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable(name = "cid")Long cid){
        List<Brand> result = brandService.queryBrandByCid(cid);
        if (result==null || CollectionUtils.isEmpty(result)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 删除品牌
     * @param bid
     * @return
     */
    @GetMapping("/bid/{bid}")
    public ResponseEntity<Void> deleteBrandByBid(@PathVariable(name = "bid")Long bid){
         brandService.deleteBrandByBid(bid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据bid查询品牌名称
     * @param bid
     * @return
     */
    @GetMapping("/{bid}")
    public ResponseEntity<Brand> queryBrandByBid(@PathVariable(name = "bid")Long bid){
        Brand brand = brandService.queryBrandByBid(bid);
        if (brand==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(brand);
    }


}
