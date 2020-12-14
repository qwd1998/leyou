package com.leyou.item.controller;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import com.leyou.item.service.CategoryService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * 根据父节点查询子节点信息
     *
     * @param pid
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Category>> queryCategoriesByPid(@RequestParam(name = "pid", defaultValue = "0") Long pid) {
        if (pid == null || pid < 0) {
            //响应400，相当于ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = categoryService.queryCategoriesByPid(pid);
        if (CollectionUtils.isEmpty(categories)) {
            // 响应404
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据bid查询分类
     *
     * @param bid
     * @return
     */
    @GetMapping("/bid/{bid}")
    public ResponseEntity<List<Category>> updateBrandByBid(@PathVariable(name = "bid") Long bid) {
        if (bid == null || bid < 0) {
            //响应400，相当于ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = brandService.queryCategoryByBid(bid);
        if (categories == null || CollectionUtils.isEmpty(categories)) {
            // 响应404
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

    /**
     * 根据ids查询出分类的名称
     * @param ids
     * @return
     */
    @GetMapping
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids) {
        List<String> categories = categoryService.queryNameByIds(ids);
        if (categories == null || CollectionUtils.isEmpty(categories)) {
            // 响应404
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(categories);
    }

}
