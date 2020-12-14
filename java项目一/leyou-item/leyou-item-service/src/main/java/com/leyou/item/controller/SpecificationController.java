package com.leyou.item.controller;

import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;


    /**
     * 根据cid查询参数组
     * @param cid
     * @return
     */
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid")Long cid){

        List<SpecGroup> groups = specificationService.queryGroupByCid(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
        return ResponseEntity.ok(groups);
    }

    /**
     * 根据gid查询规格参数
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> queryParamByGid(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "generic",required = false)Boolean generic,
            @RequestParam(value = "searching",required = false)Boolean searching
    ){
        List<SpecParam> params = specificationService.queryParamByGid(gid,cid,generic,searching);
        if (CollectionUtils.isEmpty(params)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(params);
    }

    /**
     * 根据cid查询下面的组，和组下的参数
     * @param cid
     * @return
     */
    @GetMapping("group/param/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupAndParamByCid(@PathVariable(value = "cid")Long cid){
        List<SpecGroup> groups = specificationService.queryGroupAndParamByCid(cid);
        if (CollectionUtils.isEmpty(groups)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(groups);
    }




}
